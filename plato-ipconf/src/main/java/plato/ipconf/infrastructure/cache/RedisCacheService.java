package plato.ipconf.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import plato.ipconf.domain.model.GatewayEndpoint;
import plato.ipconf.domain.service.CacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis缓存服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String ENDPOINTS_KEY = "plato:ipconf:endpoints";
    private static final String SERVICE_KEY_PREFIX = "plato:ipconf:service:";
    private static final long CACHE_TTL = 3600; // 1小时缓存过期时间

    @Override
    public List<GatewayEndpoint> getAllEndpoints() {
        try {
            List<Object> values = redisTemplate.opsForHash().values(ENDPOINTS_KEY);
            if (values != null && !values.isEmpty()) {
                return values.stream()
                        .map(obj -> (GatewayEndpoint) obj)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Failed to get all endpoints from cache", e);
        }
        return new ArrayList<>();
    }

    @Override
    public List<GatewayEndpoint> getEndpointsByService(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            String serviceKey = SERVICE_KEY_PREFIX + serviceName;
            List<Object> values = redisTemplate.opsForList().range(serviceKey, 0, -1);
            if (values != null && !values.isEmpty()) {
                return values.stream()
                        .map(obj -> (GatewayEndpoint) obj)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Failed to get endpoints for service: {}", serviceName, e);
        }
        return new ArrayList<>();
    }

    @Override
    public GatewayEndpoint getEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isEmpty()) {
            return null;
        }
        
        try {
            Object value = redisTemplate.opsForHash().get(ENDPOINTS_KEY, endpoint);
            if (value != null) {
                return (GatewayEndpoint) value;
            }
        } catch (Exception e) {
            log.error("Failed to get endpoint: {}", endpoint, e);
        }
        return null;
    }

    @Override
    public void updateEndpoints(String serviceName, List<GatewayEndpoint> endpoints) {
        if (serviceName == null || serviceName.isEmpty() || endpoints == null || endpoints.isEmpty()) {
            return;
        }
        
        try {
            // 清除服务相关的缓存
            String serviceKey = SERVICE_KEY_PREFIX + serviceName;
            redisTemplate.delete(serviceKey);
            
            // 更新端点缓存
            for (GatewayEndpoint endpoint : endpoints) {
                // 确保设置正确的服务ID
                if (endpoint.getServiceId() == null) {
                    endpoint.setServiceId(serviceName);
                }
                updateEndpoint(endpoint);
                
                // 添加到服务列表
                redisTemplate.opsForList().rightPush(serviceKey, endpoint);
            }
            
            // 设置过期时间
            redisTemplate.expire(serviceKey, CACHE_TTL, TimeUnit.SECONDS);
            
            log.info("Updated {} endpoints for service {} in cache", endpoints.size(), serviceName);
        } catch (Exception e) {
            log.error("Failed to update endpoints for service {} in cache", serviceName, e);
        }
    }

    @Override
    public void updateEndpoint(GatewayEndpoint endpoint) {
        if (endpoint == null) {
            return;
        }
        
        try {
            String endpointAddress = endpoint.getEndpoint();
            
            // 更新端点哈希表
            redisTemplate.opsForHash().put(ENDPOINTS_KEY, endpointAddress, endpoint);
            redisTemplate.expire(ENDPOINTS_KEY, CACHE_TTL, TimeUnit.SECONDS);
            
            // 更新服务列表
            if (endpoint.getServiceId() != null) {
                String serviceKey = SERVICE_KEY_PREFIX + endpoint.getServiceId();
                
                // 先检查是否已存在
                List<Object> existingEndpoints = redisTemplate.opsForList().range(serviceKey, 0, -1);
                boolean exists = false;
                
                if (existingEndpoints != null) {
                    for (int i = 0; i < existingEndpoints.size(); i++) {
                        GatewayEndpoint existing = (GatewayEndpoint) existingEndpoints.get(i);
                        if (existing.getEndpoint().equals(endpointAddress)) {
                            redisTemplate.opsForList().set(serviceKey, i, endpoint);
                            exists = true;
                            break;
                        }
                    }
                }
                
                if (!exists) {
                    redisTemplate.opsForList().rightPush(serviceKey, endpoint);
                }
                
                redisTemplate.expire(serviceKey, CACHE_TTL, TimeUnit.SECONDS);
            }
            
            log.debug("Updated endpoint in cache: {}", endpointAddress);
        } catch (Exception e) {
            log.error("Failed to update endpoint in cache: {}", endpoint.getEndpoint(), e);
        }
    }

    @Override
    public void updateConnectionCount(String endpoint, int connectionCount) {
        if (endpoint == null || endpoint.isEmpty() || connectionCount < 0) {
            return;
        }
        
        try {
            GatewayEndpoint gatewayEndpoint = getEndpoint(endpoint);
            if (gatewayEndpoint != null) {
                gatewayEndpoint.setConnectionCount(connectionCount);
                updateEndpoint(gatewayEndpoint);
                log.debug("Updated connection count for endpoint: {}, count: {}", endpoint, connectionCount);
            } else {
                log.warn("Endpoint not found in cache: {}", endpoint);
            }
        } catch (Exception e) {
            log.error("Failed to update connection count for endpoint: {}", endpoint, e);
        }
    }

    @Override
    public void updateHealthStatus(String endpoint, boolean healthy) {
        if (endpoint == null || endpoint.isEmpty()) {
            return;
        }
        
        try {
            GatewayEndpoint gatewayEndpoint = getEndpoint(endpoint);
            if (gatewayEndpoint != null) {
                gatewayEndpoint.setHealthy(healthy);
                updateEndpoint(gatewayEndpoint);
                log.debug("Updated health status for endpoint: {}, healthy: {}", endpoint, healthy);
            } else {
                log.warn("Endpoint not found in cache: {}", endpoint);
            }
        } catch (Exception e) {
            log.error("Failed to update health status for endpoint: {}", endpoint, e);
        }
    }

    @Override
    public void clearCache() {
        try {
            // 获取所有相关的键
            List<String> keys = new ArrayList<>();
            keys.add(ENDPOINTS_KEY);
            
            // 添加服务相关的键
            List<GatewayEndpoint> endpoints = getAllEndpoints();
            for (GatewayEndpoint endpoint : endpoints) {
                if (endpoint.getServiceId() != null) {
                    keys.add(SERVICE_KEY_PREFIX + endpoint.getServiceId());
                }
            }
            
            // 删除所有键
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            log.info("Cleared cache");
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
        }
    }
}