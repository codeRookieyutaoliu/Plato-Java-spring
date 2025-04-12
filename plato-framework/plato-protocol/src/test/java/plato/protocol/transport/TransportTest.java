package plato.protocol.transport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 传输层测试类
 * <p>
 * 用于测试传输层的基本功能
 * </p>
 */
public class TransportTest {
    
    private MockTcpTransport transport;
    private byte[] testData;
    
    @BeforeEach
    public void setUp() {
        transport = new MockTcpTransport();
        testData = "Hello, Transport!".getBytes();
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
     * 测试发送和接收功能
     */
    @Test
    public void testSendAndReceive() throws TransportException {
        // 连接
        transport.connect("localhost", 8080);
        
        // 发送数据
        transport.send(testData);
        assertEquals(1, transport.getSendQueueSize());
        
        // 模拟接收数据
        transport.mockReceive(testData);
        assertEquals(1, transport.getReceiveQueueSize());
        
        // 接收数据
        byte[] receivedData = transport.receive();
        assertNotNull(receivedData);
        assertArrayEquals(testData, receivedData);
        assertEquals(0, transport.getReceiveQueueSize());
    }
    
    /**
     * 测试消息处理器
     */
    @Test
    public void testMessageHandler() throws Exception {
        // 连接
        transport.connect("localhost", 8080);
        
        // 设置消息处理器
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<byte[]> receivedDataRef = new AtomicReference<>();
        
        transport.setMessageHandler(data -> {
            receivedDataRef.set(data);
            latch.countDown();
        });
        
        // 模拟接收数据
        transport.mockReceive(testData);
        
        // 等待消息处理
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        
        // 验证接收到的数据
        byte[] receivedData = receivedDataRef.get();
        assertNotNull(receivedData);
        assertArrayEquals(testData, receivedData);
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
        
        // 测试未连接时接收
        assertThrows(TransportException.class, () -> {
            transport.receive();
        });
        
        // 连接
        transport.connect("localhost", 8080);
        
        // 测试断开连接后操作
        transport.disconnect();
        
        assertThrows(TransportException.class, () -> {
            transport.send(testData);
        });
        
        assertThrows(TransportException.class, () -> {
            transport.receive();
        });
    }
    
    /**
     * 测试配置功能
     */
    @Test
    public void testConfiguration() {
        // 默认配置
        TransportConfig defaultConfig = transport.getConfig();
        assertNotNull(defaultConfig);
        
        // 设置新配置
        TransportConfig newConfig = new TransportConfig()
                .setConnectTimeout(5000)
                .setReadTimeout(6000)
                .setWriteTimeout(7000)
                .setHeartbeatInterval(30000)
                .setMaxFrameLength(2048)
                .setTcpKeepAlive(false)
                .setTcpNoDelay(false);
        
        transport.setConfig(newConfig);
        
        // 验证配置是否生效
        TransportConfig actualConfig = transport.getConfig();
        assertEquals(5000, actualConfig.getConnectTimeout());
        assertEquals(6000, actualConfig.getReadTimeout());
        assertEquals(7000, actualConfig.getWriteTimeout());
        assertEquals(30000, actualConfig.getHeartbeatInterval());
        assertEquals(2048, actualConfig.getMaxFrameLength());
        assertFalse(actualConfig.isTcpKeepAlive());
        assertFalse(actualConfig.isTcpNoDelay());
    }
} 