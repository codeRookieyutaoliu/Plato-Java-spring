package plato.common.discovery;

import java.util.List;
import java.util.Optional;

/**
 * 服务发现接口
 * 定义服务注册与发现的抽象接口，遵循依赖倒置原则
 * 具体实现可以是Eureka、Consul、Nacos等
 */
public interface ServiceDiscovery {
    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param serviceInstance 服务实例信息
     */
    void register(String serviceName, ServiceInstance serviceInstance);

    /**
     * 注销服务
     * @param serviceName 服务名称
     * @param serviceInstance 服务实例信息
     */
    void deregister(String serviceName, ServiceInstance serviceInstance);

    /**
     * 获取服务实例列表
     * @param serviceName 服务名称
     * @return 服务实例列表
     */
    List<ServiceInstance> getInstances(String serviceName);

    /**
     * 获取单个服务实例（可用于负载均衡）
     * @param serviceName 服务名称
     * @return 服务实例
     */
    Optional<ServiceInstance> getInstance(String serviceName);
}