package plato.message.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import plato.message.application.registry.MessageHandlerRegistry;
import plato.message.application.service.MessageProcessingService;
import plato.message.application.handler.HeartbeatMessageHandler;
import plato.message.application.handler.LoginMessageHandler;

/**
 * 消息模块配置类
 * <p>
 * 提供消息模块所需的Bean配置
 * </p>
 */
@Configuration
@ComponentScan(basePackages = {
        "plato.message.application.api",
        "plato.message.application.service.impl" 
})
public class MessageConfig {
    
    /**
     * 创建消息处理器注册表Bean
     *
     * @return 消息处理器注册表
     */
    @Bean
    public MessageHandlerRegistry messageHandlerRegistry() {
        return new MessageHandlerRegistry();
    }
    
    /**
     * 创建消息处理服务Bean
     *
     * @param registry 消息处理器注册表
     * @return 消息处理服务
     */
    @Bean
    public MessageProcessingService messageProcessingService(MessageHandlerRegistry registry) {
        MessageProcessingService service = new MessageProcessingService(registry);
        
        // 注册默认的消息处理器
        service.registerHandler(heartbeatMessageHandler());
        service.registerHandler(loginMessageHandler());
        
        return service;
    }
    
    /**
     * 创建心跳消息处理器Bean
     *
     * @return 心跳消息处理器
     */
    @Bean
    public HeartbeatMessageHandler heartbeatMessageHandler() {
        return new HeartbeatMessageHandler();
    }
    
    /**
     * 创建登录消息处理器Bean
     *
     * @return 登录消息处理器
     */
    @Bean
    public LoginMessageHandler loginMessageHandler() {
        return new LoginMessageHandler();
    }
}