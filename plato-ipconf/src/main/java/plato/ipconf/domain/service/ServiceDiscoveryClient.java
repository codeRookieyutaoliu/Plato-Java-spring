package plato.ipconf.domain.service;

import plato.ipconf.domain.model.GatewayEndpoint;

import java.util.List;

/**
 * 服务发现客户端接口
 * 定义服务发现的核心功能
 * 对应Go版本中的source包
 */
public interface ServiceDiscoveryClient {
    
    /**
     * 获取所有可用服务
     * 
     * @return 服务名称列表
     */
    List<String> getServices();
    
    /**
     * 获取指定服务的所有实例
     * 
     * @param serviceName 服务名称
     * @return 网关端点列表
     */
    List<GatewayEndpoint> getServiceInstances(String serviceName);
    
    /**
     * 注册服务实例
     * 
     * @param serviceName 服务名称
     * @param host 主机名或IP地址
     * @param port 端口
     * @param metadata 元数据
     * @return 是否注册成功
     */
    boolean registerInstance(String serviceName, String host, int port, java.util.Map<String, String> metadata);
    
    /**
     * 注销服务实例
     * 
     * @param serviceName 服务名称
     * @param host 主机名或IP地址
     * @param port 端口
     * @return 是否注销成功
     */
    boolean deregisterInstance(String serviceName, String host, int port);
    
    /**
     * 关闭服务发现客户端
     */
    void close();
} 