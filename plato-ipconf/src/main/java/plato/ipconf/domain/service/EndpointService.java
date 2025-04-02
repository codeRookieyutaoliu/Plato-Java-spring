package plato.ipconf.domain.service;

import plato.ipconf.domain.model.ClientInfo;
import plato.ipconf.domain.model.GatewayEndpoint;

import java.util.List;

/**
 * 端点服务接口
 * 定义获取和管理网关端点的核心功能
 */
public interface EndpointService {
    
    /**
     * 获取所有可用的网关端点
     * 
     * @return 网关端点列表
     */
    List<GatewayEndpoint> getAllEndpoints();
    
    /**
     * 获取指定服务的所有可用端点
     * 
     * @param serviceName 服务名称
     * @return 网关端点列表
     */
    List<GatewayEndpoint> getEndpointsByService(String serviceName);
    
    /**
     * 获取客户端的最佳网关端点
     * 
     * @param clientInfo 客户端信息
     * @return 最佳网关端点列表，按优先级排序
     */
    List<GatewayEndpoint> getBestEndpoints(ClientInfo clientInfo);
    
    /**
     * 获取客户端的最佳网关端点
     * 
     * @param clientInfo 客户端信息
     * @param limit 返回的端点数量限制
     * @return 最佳网关端点列表，按优先级排序
     */
    List<GatewayEndpoint> getBestEndpoints(ClientInfo clientInfo, int limit);
    
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
     * 刷新端点信息
     * 从服务发现和其他数据源重新加载端点信息
     */
    void refreshEndpoints();
} 