package plato.gateway.infrastructure.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Nacos 配置类
 * 启用配置自动刷新
 */
@Configuration
public class NacosConfig {
    
    /**
     * 在需要动态刷新配置的 Bean 上添加 @RefreshScope 注解
     * 例如：
     * @Bean
     * @RefreshScope
     * public SomeBean someBean() {
     *     return new SomeBean();
     * }
     */
} 