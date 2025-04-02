package plato.ipconf.infrastructure.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import plato.ipconf.domain.model.GatewayEndpoint;
import plato.ipconf.domain.model.GeoLocation;
import plato.ipconf.domain.service.GeoLocationService;
import plato.ipconf.domain.service.ServiceDiscoveryClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nacos服务发现客户端实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NacosServiceDiscoveryClient implements ServiceDiscoveryClient {

    private final NacosServiceManager nacosServiceManager;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final GeoLocationService geoLocationService;
    
    @Value("${plato.ipconf.gateway-service-name:plato-gateway}")
    private String gatewayServiceName;
    
    @Value("${plato.ipconf.gateway-tcp-port:8900}")
    private int gatewayTcpPort;

    @Override
    public List<String> getServices() {
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            return namingService.getServicesOfServer(1, Integer.MAX_VALUE).getData();
        } catch (NacosException e) {
            log.error("Failed to get services from Nacos", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<GatewayEndpoint> getServiceInstances(String serviceName) {
        List<GatewayEndpoint> endpoints = new ArrayList<>();
        
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            List<Instance> instances = namingService.selectInstances(serviceName, true);
            
            for (Instance instance : instances) {
                GatewayEndpoint endpoint = convertInstanceToEndpoint(instance);
                if (endpoint != null) {
                    endpoints.add(endpoint);
                }
            }
            
            log.info("Found {} gateway instances for service: {}", endpoints.size(), serviceName);
        } catch (NacosException e) {
            log.error("Failed to get instances from Nacos for service: {}", serviceName, e);
        }
        
        return endpoints;
    }

    @Override
    public boolean registerInstance(String serviceName, String host, int port, Map<String, String> metadata) {
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            Instance instance = new Instance();
            instance.setIp(host);
            instance.setPort(port);
            instance.setMetadata(metadata);
            instance.setHealthy(true);
            
            namingService.registerInstance(serviceName, instance);
            log.info("Successfully registered instance: {}:{} for service: {}", host, port, serviceName);
            return true;
        } catch (NacosException e) {
            log.error("Failed to register instance: {}:{} for service: {}", host, port, serviceName, e);
            return false;
        }
    }
    
    @Override
    public boolean deregisterInstance(String serviceName, String host, int port) {
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            namingService.deregisterInstance(serviceName, host, port);
            log.info("Successfully deregistered instance: {}:{} for service: {}", host, port, serviceName);
            return true;
        } catch (NacosException e) {
            log.error("Failed to deregister instance: {}:{} for service: {}", host, port, serviceName, e);
            return false;
        }
    }
    
    @Override
    public void close() {
        // Nacos客户端不需要显式关闭
        log.debug("Nacos client does not need to be explicitly closed");
    }
    
    /**
     * 检查实例是否健康
     * 注意：这是一个自定义方法，不是接口要求的
     */
    public boolean isInstanceHealthy(String endpoint) {
        String[] parts = endpoint.split(":");
        if (parts.length != 2) {
            log.warn("Invalid endpoint format: {}", endpoint);
            return false;
        }
        
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        
        try {
            NamingService namingService = nacosServiceManager.getNamingService();
            List<Instance> instances = namingService.selectInstances(gatewayServiceName, true);
            
            for (Instance instance : instances) {
                if (instance.getIp().equals(host) && instance.getPort() == port) {
                    return instance.isHealthy();
                }
            }
            
            log.warn("Instance not found in Nacos: {}", endpoint);
            return false;
        } catch (NacosException e) {
            log.error("Failed to check instance health: {}", endpoint, e);
            return false;
        }
    }
    
    /**
     * 获取所有网关实例
     * 注意：这是一个自定义方法，不是接口要求的
     */
    public List<GatewayEndpoint> getAllGatewayInstances() {
        return getServiceInstances(gatewayServiceName);
    }
    
    /**
     * 获取指定服务的网关实例
     * 注意：这是一个自定义方法，保留原有功能
     */
    public List<GatewayEndpoint> getGatewayInstancesByService(String serviceName) {
        return getServiceInstances(serviceName);
    }
    
    /**
     * 将Nacos实例转换为网关端点
     */
    private GatewayEndpoint convertInstanceToEndpoint(Instance instance) {
        try {
            String host = instance.getIp();
            int port = gatewayTcpPort; // 使用网关TCP端口，而不是HTTP端口
            
            // 从元数据中获取权重
            Map<String, String> metadata = instance.getMetadata();
            int weight = metadata.containsKey("weight") ? 
                    Integer.parseInt(metadata.getOrDefault("weight", "100")) : 100;
            
            // 获取地理位置信息
            GeoLocation geoLocation = geoLocationService.getGeoLocation(host);
            
            return GatewayEndpoint.builder()
                    .serviceId(instance.getServiceName())
                    .host(host)
                    .port(port)
                    .weight(weight)
                    .connectionCount(0) // 初始连接数为0
                    .geoLocation(geoLocation)
                    .healthy(instance.isHealthy())
                    .build();
        } catch (Exception e) {
            log.error("Failed to convert instance to endpoint: {}", instance, e);
            return null;
        }
    }
}