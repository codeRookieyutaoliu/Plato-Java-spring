package plato.protocol.transport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 基本传输层测试
 * <p>
 * 测试传输层基本功能，不依赖于protobuf生成的类
 * </p>
 */
public class BasicTransportTest {
    
    private MockTcpTransport transport;
    private byte[] testData;
    
    @BeforeEach
    public void setUp() {
        transport = new MockTcpTransport();
        testData = "Hello, Transport Layer!".getBytes();
    }
    
    /**
     * 测试连接和断开功能
     */
    @Test
    public void testConnectAndDisconnect() throws TransportException {
        // 初始状态
        assertEquals(Transport.TransportState.INITIALIZED, transport.getState());
        assertFalse(transport.isConnected());
        
        // 连接
        transport.connect("localhost", 8080);
        assertEquals(Transport.TransportState.CONNECTED, transport.getState());
        assertTrue(transport.isConnected());
        
        // 断开连接
        transport.disconnect();
        assertEquals(Transport.TransportState.DISCONNECTED, transport.getState());
        assertFalse(transport.isConnected());
    }
    
    /**
     * 测试异步连接和断开功能
     */
    @Test
    public void testAsyncConnectAndDisconnect() throws Exception {
        // 异步连接
        CompletableFuture<Void> connectFuture = transport.connectAsync("localhost", 8080);
        connectFuture.get(1, TimeUnit.SECONDS); // 等待连接完成
        
        assertEquals(Transport.TransportState.CONNECTED, transport.getState());
        assertTrue(transport.isConnected());
        
        // 异步断开连接
        CompletableFuture<Void> disconnectFuture = transport.disconnectAsync();
        disconnectFuture.get(1, TimeUnit.SECONDS); // 等待断开连接完成
        
        assertEquals(Transport.TransportState.DISCONNECTED, transport.getState());
        assertFalse(transport.isConnected());
    }
    
    /**
     * 测试消息发送和接收
     */
    @Test
    public void testSendAndReceive() throws TransportException {
        // 连接
        transport.connect("localhost", 8080);
        
        // 发送消息
        transport.send(testData);
        
        // 设置接收处理器
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<byte[]> receivedDataRef = new AtomicReference<>();
        
        transport.setMessageHandler(data -> {
            receivedDataRef.set(data);
            latch.countDown();
        });
        
        // 模拟接收消息
        transport.mockReceive(testData);
        
        // 验证接收结果
        byte[] receivedData = receivedDataRef.get();
        assertNotNull(receivedData);
        assertArrayEquals(testData, receivedData);
    }
    
    /**
     * 测试异步消息发送和接收
     */
    @Test
    public void testAsyncSendAndReceive() throws Exception {
        // 连接
        transport.connect("localhost", 8080);
        
        // 设置接收处理器
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<byte[]> receivedDataRef = new AtomicReference<>();
        
        transport.setMessageHandler(data -> {
            receivedDataRef.set(data);
            latch.countDown();
        });
        
        // 异步发送消息
        CompletableFuture<Void> sendFuture = transport.sendAsync(testData);
        sendFuture.get(1, TimeUnit.SECONDS); // 等待发送完成
        
        // 验证接收结果
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        byte[] receivedData = receivedDataRef.get();
        assertNotNull(receivedData);
        assertArrayEquals(testData, receivedData);
    }
    
    /**
     * 测试配置
     */
    @Test
    public void testTransportConfig() {
        // 创建配置
        TransportConfig config = TransportConfig.createDefault()
                .setConnectTimeout(5000)
                .setReadTimeout(10000)
                .setWriteTimeout(3000)
                .setHeartbeatInterval(30000)
                .setMaxFrameLength(8192)
                .setTcpKeepAlive(false)
                .setTcpNoDelay(false);
        
        // 设置配置
        transport.setConfig(config);
        
        // 验证配置
        TransportConfig retrievedConfig = transport.getConfig();
        assertEquals(5000, retrievedConfig.getConnectTimeout());
        assertEquals(10000, retrievedConfig.getReadTimeout());
        assertEquals(3000, retrievedConfig.getWriteTimeout());
        assertEquals(30000, retrievedConfig.getHeartbeatInterval());
        assertEquals(8192, retrievedConfig.getMaxFrameLength());
        assertFalse(retrievedConfig.isTcpKeepAlive());
        assertFalse(retrievedConfig.isTcpNoDelay());
    }
    
    /**
     * 测试异常处理
     */
    @Test
    public void testExceptionHandling() {
        // 测试未连接时发送
        assertThrows(TransportException.class, () -> {
            transport.send(testData);
        });
        
        // 连接后断开
        transport.connect("localhost", 8080);
        transport.disconnect();
        
        // 测试断开连接后发送
        assertThrows(TransportException.class, () -> {
            transport.send(testData);
        });
        
        // 测试连接已关闭异常
        TransportException exception = assertThrows(TransportException.class, () -> {
            transport.send(testData);
        });
        assertEquals(TransportException.TransportExceptionType.CONNECTION_CLOSED, exception.getType());
    }
} 