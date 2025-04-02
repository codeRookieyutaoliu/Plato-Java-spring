package plato.common.discovery;

import java.util.List;

/**
 * 服务发现接口
 * 定义服务发现的核心功能，包括服务注册、发现和健康检查
 */
public interface DiscoveryService {
    /**
     * 注册服务实例
     * @param serviceName 服务名称
     * @param instanceId 实例ID
     * @param metadata 服务元数据
     */
    void registerService(String serviceName, String instanceId, java.util.Map<String, String> metadata);

    /**
     * 注销服务实例
     * @param serviceName 服务名称
     * @param instanceId 实例ID
     */
    void deregisterService(String serviceName, String instanceId);

    /**
     * 获取服务实例列表
     * @param serviceName 服务名称
     * @return 服务实例列表
     */
    List<ServiceInstance> getServiceInstances(String serviceName);

    /**
     * 服务实例信息
     */
    interface ServiceInstance {
        String getInstanceId();
        String getServiceName();
        String getHost();
        int getPort();
        java.util.Map<String, String> getMetadata();
        boolean isHealthy();
    }
}