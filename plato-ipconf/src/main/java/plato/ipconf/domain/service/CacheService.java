package plato.ipconf.domain.service;

import plato.ipconf.domain.model.GatewayEndpoint;

import java.util.List;

/**
 * 缓存服务接口
 * 定义端点缓存的核心功能
 * 对应Go版本中的domain包中的缓存功能
 */
public interface CacheService {
    
    /**
     * 获取所有缓存的端点
     * 
     * @return 端点列表
     */
    List<GatewayEndpoint> getAllEndpoints();
    
    /**
     * 获取指定服务的所有端点
     * 
     * @param serviceName 服务名称
     * @return 端点列表
     */
    List<GatewayEndpoint> getEndpointsByService(String serviceName);
    
    /**
     * 获取指定端点
     * 
     * @param endpoint 端点地址（格式：host:port）
     * @return 端点对象
     */
    GatewayEndpoint getEndpoint(String endpoint);
    
    /**
     * 更新服务的端点列表
     * 
     * @param serviceName 服务名称
     * @param endpoints 端点列表
     */
    void updateEndpoints(String serviceName, List<GatewayEndpoint> endpoints);
    
    /**
     * 更新单个端点
     * 
     * @param endpoint 端点对象
     */
    void updateEndpoint(GatewayEndpoint endpoint);
    
    /**
     * 更新端点的连接数
     * 
     * @param endpoint 端点地址（格式：host:port）
     * @param connectionCount 连接数
     */
    void updateConnectionCount(String endpoint, int connectionCount);
    
    /**
     * 更新端点的健康状态
     * 
     * @param endpoint 端点地址（格式：host:port）
     * @param healthy 是否健康
     */
    void updateHealthStatus(String endpoint, boolean healthy);
    
    /**
     * 清除所有缓存
     */
    void clearCache();
} 