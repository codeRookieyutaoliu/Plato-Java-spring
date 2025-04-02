package plato.ipconf.application;

import plato.ipconf.domain.model.ClientInfo;
import plato.ipconf.domain.model.GatewayEndpoint;

import java.util.List;

/**
 * IP配置应用服务接口
 * 定义IP配置服务的业务功能
 */
public interface IpConfService {
    
    /**
     * 获取所有网关端点
     * 
     * @return 网关端点列表
     */
    List<GatewayEndpoint> getAllEndpoints();
    
    /**
     * 获取指定服务的网关端点
     * 
     * @param serviceName 服务名称
     * @return 网关端点列表
     */
    List<GatewayEndpoint> getEndpointsByService(String serviceName);
    
    /**
     * 获取最佳网关端点
     * 
     * @param clientInfo 客户端信息
     * @return 最佳网关端点列表
     */
    List<GatewayEndpoint> getBestEndpoints(ClientInfo clientInfo);
    
    /**
     * 获取最佳网关端点，限制返回数量
     * 
     * @param clientInfo 客户端信息
     * @param limit 返回的端点数量限制
     * @return 最佳网关端点列表
     */
    List<GatewayEndpoint> getBestEndpoints(ClientInfo clientInfo, int limit);
    
    /**
     * 更新端点连接数
     * 
     * @param endpoint 端点地址 (host:port)
     * @param connectionCount 连接数
     */
    void updateConnectionCount(String endpoint, int connectionCount);
    
    /**
     * 更新端点健康状态
     * 
     * @param endpoint 端点地址 (host:port)
     * @param healthy 健康状态
     */
    void updateHealthStatus(String endpoint, boolean healthy);
    
    /**
     * 刷新端点信息
     */
    void refreshEndpoints();
} 