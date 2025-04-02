package plato.client.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import plato.common.sdk.ChatConnection;
import plato.common.sdk.Message;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ChatClientImpl单元测试
 */
class ChatClientImplTest {
    private ChatClientImpl chatClient;
    
    @Mock
    private ChatConnection mockConnection;
    
    private final String nickname = "testUser";
    private final String userId = "123456";
    private final String sessionId = "session123";
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatClient = new ChatClientImpl(nickname, userId, sessionId, mockConnection);
    }
    
    @Test
    void connect_shouldReturnTrueWhenConnectionSucceeds() {
        // 设置模拟对象的行为
        when(mockConnection.connect()).thenReturn(true);
        when(mockConnection.send(any(Message.class))).thenReturn(true);
        
        // 执行测试
        boolean result = chatClient.connect();
        
        // 验证结果
        assertTrue(result);
        verify(mockConnection).connect();
        verify(mockConnection).send(any(Message.class));
        verify(mockConnection).receive(any(Consumer.class));
    }
    
    @Test
    void connect_shouldReturnFalseWhenConnectionFails() {
        // 设置模拟对象的行为
        when(mockConnection.connect()).thenReturn(false);
        
        // 执行测试
        boolean result = chatClient.connect();
        
        // 验证结果
        assertFalse(result);
        verify(mockConnection).connect();
        verify(mockConnection, never()).send(any(Message.class));
    }
    
    @Test
    void reconnect_shouldReturnTrueWhenReconnectionSucceeds() {
        // 设置模拟对象的行为
        when(mockConnection.connect()).thenReturn(true);
        when(mockConnection.send(any(Message.class))).thenReturn(true);
        
        // 执行测试
        boolean result = chatClient.reconnect();
        
        // 验证结果
        assertTrue(result);
        verify(mockConnection).close();
        verify(mockConnection).connect();
        verify(mockConnection).send(any(Message.class));
    }
    
    @Test
    void send_shouldReturnTrueWhenSendSucceeds() {
        // 设置模拟对象的行为
        when(mockConnection.isConnected()).thenReturn(true);
        when(mockConnection.send(any(Message.class))).thenReturn(true);
        
        // 创建测试消息
        Message message = Message.builder()
                .type(Message.TYPE_TEXT)
                .content("Hello, world!")
                .build();
        
        // 执行测试
        boolean result = chatClient.send(message);
        
        // 验证结果
        assertTrue(result);
        verify(mockConnection).send(any(Message.class));
        
        // 验证消息的发送者信息是否已设置
        assertEquals(nickname, message.getName());
        assertEquals(userId, message.getFromUserId());
        assertEquals(sessionId, message.getSessionId());
    }
    
    @Test
    void send_shouldReturnFalseWhenNotConnected() {
        // 设置模拟对象的行为
        when(mockConnection.isConnected()).thenReturn(false);
        
        // 创建测试消息
        Message message = Message.builder()
                .type(Message.TYPE_TEXT)
                .content("Hello, world!")
                .build();
        
        // 执行测试
        boolean result = chatClient.send(message);
        
        // 验证结果
        assertFalse(result);
        verify(mockConnection, never()).send(any(Message.class));
    }
    
    @Test
    void close_shouldCloseConnection() {
        // 执行测试
        chatClient.close();
        
        // 验证结果
        verify(mockConnection).close();
    }
    
    @Test
    void isConnected_shouldReturnConnectionStatus() {
        // 设置模拟对象的行为
        when(mockConnection.isConnected()).thenReturn(true);
        
        // 执行测试
        boolean result = chatClient.isConnected();
        
        // 验证结果
        assertTrue(result);
        verify(mockConnection).isConnected();
    }
} 