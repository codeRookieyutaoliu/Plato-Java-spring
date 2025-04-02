package plato.client.domain.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import plato.common.sdk.ChatClient;
import plato.common.sdk.ChatConnection;
import plato.common.sdk.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 聊天客户端实现类
 * 实现ChatClient接口，提供聊天客户端的核心功能
 */
@Slf4j
public class ChatClientImpl implements ChatClient {
    @Getter
    private final String nickname;
    @Getter
    private final String userId;
    @Getter
    private final String sessionId;
    private final ChatConnection connection;
    private final Map<String, Long> clientIdTable = new ConcurrentHashMap<>();
    private final AtomicLong currentClientId = new AtomicLong(0);
    private Consumer<Message> messageConsumer;
    private volatile boolean closed = false;

    /**
     * 构造函数
     *
     * @param nickname  用户昵称
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param connection 连接对象
     */
    public ChatClientImpl(String nickname, String userId, String sessionId, ChatConnection connection) {
        this.nickname = nickname;
        this.userId = userId;
        this.sessionId = sessionId;
        this.connection = connection;
    }

    @Override
    public boolean connect() {
        if (closed) {
            log.warn("客户端已关闭，无法连接");
            return false;
        }

        boolean success = connection.connect();
        if (success) {
            // 发送登录消息
            Message loginMessage = Message.builder()
                    .type(Message.TYPE_LOGIN)
                    .name(nickname)
                    .fromUserId(userId)
                    .sessionId(sessionId)
                    .build();
            connection.send(loginMessage);
            
            // 设置消息接收处理
            connection.receive(message -> {
                if (messageConsumer != null) {
                    messageConsumer.accept(message);
                }
            });
        }
        return success;
    }

    @Override
    public boolean reconnect() {
        if (closed) {
            log.warn("客户端已关闭，无法重连");
            return false;
        }

        // 先关闭现有连接
        connection.close();
        
        // 重新连接
        boolean success = connection.connect();
        if (success) {
            // 发送重连消息
            Message reconnMessage = Message.builder()
                    .type(Message.TYPE_RECONNECT)
                    .name(nickname)
                    .fromUserId(userId)
                    .sessionId(sessionId)
                    .build();
            connection.send(reconnMessage);
        }
        return success;
    }

    @Override
    public void close() {
        if (!closed) {
            connection.close();
            closed = true;
            log.info("客户端已关闭");
        }
    }

    @Override
    public boolean send(Message message) {
        if (closed || !connection.isConnected()) {
            log.warn("客户端未连接或已关闭，无法发送消息");
            return false;
        }

        // 设置发送者信息
        message.setName(nickname);
        message.setFromUserId(userId);
        message.setSessionId(sessionId);
        
        // 记录客户端ID
        long clientId = currentClientId.incrementAndGet();
        clientIdTable.put(message.getSessionId(), clientId);
        
        return connection.send(message);
    }

    @Override
    public void receive(Consumer<Message> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public long getCurrentClientId() {
        return clientIdTable.getOrDefault(sessionId, 0L);
    }

    @Override
    public long getConnectionId() {
        return connection.getConnectionId();
    }

    @Override
    public boolean isConnected() {
        return !closed && connection.isConnected();
    }
} 