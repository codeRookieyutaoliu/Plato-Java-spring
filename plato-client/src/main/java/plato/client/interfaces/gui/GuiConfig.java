package plato.client.interfaces.gui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plato.client.infrastructure.adapter.ChatClientFactoryImpl;
import plato.common.sdk.ChatClient;
import plato.common.sdk.ChatClientFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * GUI配置类
 * 配置GUI模式
 */
@Configuration
@ConditionalOnProperty(name = "plato.client.mode", havingValue = "gui")
public class GuiConfig {
    @Value("${plato.client.host:127.0.0.1}")
    private String host;
    
    @Value("${plato.client.port:8900}")
    private int port;
    
    @Value("${plato.client.nickname:user}")
    private String nickname;
    
    @Value("${plato.client.user-id:}")
    private String userId;
    
    @Value("${plato.client.session-id:}")
    private String sessionId;
    
    /**
     * 创建ChatClient bean
     *
     * @return ChatClient实例
     * @throws UnknownHostException 如果无法解析主机名
     */
    @Bean
    public ChatClient chatClient() throws UnknownHostException {
        // 如果未指定用户ID，生成一个随机ID
        if (userId == null || userId.isEmpty()) {
            userId = UUID.randomUUID().toString().replace("-", "");
        }

        // 如果未指定会话ID，使用用户ID作为会话ID
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = userId;
        }

        // 创建客户端
        ChatClientFactory factory = new ChatClientFactoryImpl();
        return factory.createChatClient(
            InetAddress.getByName(host),
            port,
            nickname,
            userId,
            sessionId
        );
    }
} 