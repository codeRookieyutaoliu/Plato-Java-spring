package plato.common.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;

/**
 * Nacos配置类
 * 启用服务发现和配置中心
 */
@Configuration
@EnableDiscoveryClient
public class NacosConfig {
    // Nacos配置由Spring Cloud Alibaba自动配置
} 