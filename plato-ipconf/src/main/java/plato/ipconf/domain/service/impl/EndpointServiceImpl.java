package plato.ipconf.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import plato.ipconf.domain.model.ClientInfo;
import plato.ipconf.domain.model.GatewayEndpoint;
import plato.ipconf.domain.service.CacheService;
import plato.ipconf.domain.service.EndpointService;
import plato.ipconf.domain.service.LoadBalancerService;
import plato.ipconf.domain.service.ServiceDiscoveryClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 端点服务实现
 * 实现了端点的获取、管理和调度功能
 * 对应Go版本中的Dispatcher结构体
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EndpointServiceImpl implements EndpointService {

    private final ServiceDiscoveryClient serviceDiscoveryClient;
    private final CacheService cacheService;
    private final LoadBalancerService loadBalancerService;
    
    /**
     * 候选节点表，key为"ip:port"格式
     * 对应Go版本中的candidateTable
     */
    private final Map<String, GatewayEndpoint> candidateTable = new ConcurrentHashMap<>();

    @Override
    public List<GatewayEndpoint> getAllEndpoints() {
        List<GatewayEndpoint> cachedEndpoints = cacheService.getAllEndpoints();
        
        // 如果缓存为空，则从服务发现获取并更新缓存
        if (cachedEndpoints == null || cachedEndpoints.isEmpty()) {
            log.info("Cache is empty, refreshing endpoints from service discovery");
            refreshEndpoints();
            cachedEndpoints = cacheService.getAllEndpoints();
        }
        
        return cachedEndpoints != null ? cachedEndpoints : new ArrayList<>();
    }

    @Override
    public List<GatewayEndpoint> getEndpointsByService(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            log.warn("Service name is null or empty");
            return new ArrayList<>();
        }
        
        List<GatewayEndpoint> cachedEndpoints = cacheService.getEndpointsByService(serviceName);
        
        // 如果缓存为空，则从服务发现获取并更新缓存
        if (cachedEndpoints == null || cachedEndpoints.isEmpty()) {
            log.info("Cache is empty for service {}, refreshing endpoints from service discovery", serviceName);
            List<GatewayEndpoint> discoveredEndpoints = serviceDiscoveryClient.getServiceInstances(serviceName);
            if (discoveredEndpoints != null && !discoveredEndpoints.isEmpty()) {
                cacheService.updateEndpoints(serviceName, discoveredEndpoints);
                cachedEndpoints = discoveredEndpoints;
            }
        }
        
        return cachedEndpoints != null ? cachedEndpoints : new ArrayList<>();
    }

    @Override
    public List<GatewayEndpoint> getBestEndpoints(ClientInfo clientInfo) {
        return getBestEndpoints(clientInfo, 3); // 默认返回3个端点
    }

    @Override
    public List<GatewayEndpoint> getBestEndpoints(ClientInfo clientInfo, int limit) {
        if (clientInfo == null) {
            log.warn("Client info is null");
            clientInfo = new ClientInfo(); // 使用默认值
        }
        
        if (limit <= 0) {
            limit = 3; // 默认返回3个端点
        }
        
        // 获取所有端点
        List<GatewayEndpoint> allEndpoints = getAllEndpoints();
        
        // 使用负载均衡服务对端点进行排序和筛选
        return loadBalancerService.getBestEndpoints(allEndpoints, clientInfo, limit);
    }

    @Override
    public void updateConnectionCount(String endpoint, int connectionCount) {
        if (endpoint == null || endpoint.isEmpty()) {
            log.warn("Endpoint is null or empty");
            return;
        }
        
        if (connectionCount < 0) {
            log.warn("Connection count is negative: {}", connectionCount);
            connectionCount = 0;
        }
        
        log.debug("Updating connection count for endpoint {}: {}", endpoint, connectionCount);
        cacheService.updateConnectionCount(endpoint, connectionCount);
        
        // 同时更新候选节点表
        GatewayEndpoint gatewayEndpoint = cacheService.getEndpoint(endpoint);
        if (gatewayEndpoint != null) {
            candidateTable.put(endpoint, gatewayEndpoint);
        }
    }

    @Override
    public void updateHealthStatus(String endpoint, boolean healthy) {
        if (endpoint == null || endpoint.isEmpty()) {
            log.warn("Endpoint is null or empty");
            return;
        }
        
        log.debug("Updating health status for endpoint {}: {}", endpoint, healthy);
        cacheService.updateHealthStatus(endpoint, healthy);
        
        // 同时更新候选节点表
        GatewayEndpoint gatewayEndpoint = cacheService.getEndpoint(endpoint);
        if (gatewayEndpoint != null) {
            candidateTable.put(endpoint, gatewayEndpoint);
        }
    }

    @Override
    @Scheduled(fixedRateString = "${plato.ipconf.refresh.interval:60000}")
    public void refreshEndpoints() {
        log.info("Refreshing endpoints from service discovery");
        
        // 从服务发现获取所有服务
        List<String> services = serviceDiscoveryClient.getServices();
        
        if (services == null || services.isEmpty()) {
            log.warn("No services found from service discovery");
            return;
        }
        
        // 更新每个服务的端点
        for (String service : services) {
            List<GatewayEndpoint> discoveredEndpoints = serviceDiscoveryClient.getServiceInstances(service);
            
            if (discoveredEndpoints == null || discoveredEndpoints.isEmpty()) {
                log.warn("No endpoints found for service: {}", service);
                continue;
            }
            
            log.info("Found {} endpoints for service: {}", discoveredEndpoints.size(), service);
            
            // 保留现有的连接数信息
            List<GatewayEndpoint> cachedEndpoints = cacheService.getEndpointsByService(service);
            if (cachedEndpoints != null && !cachedEndpoints.isEmpty()) {
                Map<String, GatewayEndpoint> cachedEndpointMap = new ConcurrentHashMap<>();
                for (GatewayEndpoint endpoint : cachedEndpoints) {
                    cachedEndpointMap.put(endpoint.getEndpoint(), endpoint);
                }
                
                // 更新发现的端点的连接数
                for (GatewayEndpoint discoveredEndpoint : discoveredEndpoints) {
                    GatewayEndpoint cachedEndpoint = cachedEndpointMap.get(discoveredEndpoint.getEndpoint());
                    if (cachedEndpoint != null) {
                        discoveredEndpoint.setConnectionCount(cachedEndpoint.getConnectionCount());
                    }
                }
            }
            
            // 更新缓存
            cacheService.updateEndpoints(service, discoveredEndpoints);
            
            // 更新候选节点表
            for (GatewayEndpoint endpoint : discoveredEndpoints) {
                candidateTable.put(endpoint.getEndpoint(), endpoint);
            }
        }
    }
} 