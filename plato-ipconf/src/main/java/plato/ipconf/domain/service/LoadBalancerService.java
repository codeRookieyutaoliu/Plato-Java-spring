package plato.ipconf.domain.service;

import plato.ipconf.domain.model.ClientInfo;
import plato.ipconf.domain.model.EndpointScore;
import plato.ipconf.domain.model.GatewayEndpoint;

import java.util.List;

/**
 * 负载均衡服务接口
 * 定义负载均衡算法和评分功能
 */
public interface LoadBalancerService {
    
    /**
     * 计算端点的评分
     * 
     * @param endpoint 网关端点
     * @param clientInfo 客户端信息
     * @return 端点评分
     */
    EndpointScore calculateScore(GatewayEndpoint endpoint, ClientInfo clientInfo);
    
    /**
     * 对端点列表进行评分并排序
     * 
     * @param endpoints 网关端点列表
     * @param clientInfo 客户端信息
     * @return 排序后的端点评分列表
     */
    List<EndpointScore> rankEndpoints(List<GatewayEndpoint> endpoints, ClientInfo clientInfo);
    
    /**
     * 获取最佳端点列表
     * 
     * @param endpoints 网关端点列表
     * @param clientInfo 客户端信息
     * @param limit 返回的端点数量限制
     * @return 最佳端点列表
     */
    List<GatewayEndpoint> getBestEndpoints(List<GatewayEndpoint> endpoints, ClientInfo clientInfo, int limit);
} 