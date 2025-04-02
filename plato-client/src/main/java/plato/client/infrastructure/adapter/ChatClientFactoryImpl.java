package plato.client.infrastructure.adapter;

import plato.client.domain.service.ChatClientImpl;
import plato.common.sdk.ChatClient;
import plato.common.sdk.ChatClientFactory;
import plato.common.sdk.ChatConnection;

import java.net.InetAddress;

/**
 * 聊天客户端工厂实现类
 * 实现ChatClientFactory接口，用于创建ChatClient实例
 */
public class ChatClientFactoryImpl implements ChatClientFactory {
    @Override
    public ChatClient createChatClient(InetAddress ip, int port, String nickname, String userId, String sessionId) {
        // 创建连接对象
        ChatConnection connection = new NettyChatConnection(ip.getHostAddress(), port);
        
        // 创建客户端对象
        return new ChatClientImpl(nickname, userId, sessionId, connection);
    }
} 