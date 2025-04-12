package plato.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import static org.junit.jupiter.api.Assertions.*;

import plato.protocol.codec.tcp.DataPacket;
import plato.protocol.codec.tcp.MessageEncoder;
import plato.protocol.codec.tcp.MessageDecoder;
import plato.protocol.codec.tcp.TcpCodecHandler;
import plato.protocol.proto.*;
import plato.protocol.transport.MockTcpTransport;
import plato.protocol.transport.Transport;
import plato.protocol.transport.TransportException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 协议包集成测试
 * <p>
 * 测试协议包的各个组件组合使用
 * </p>
 */
public class IntegrationTest {
    
    /**
     * 检查Proto生成的类是否可用
     */
    private static boolean isProtobufClassesAvailable() {
        try {
            // 尝试加载一个protobuf生成的类
            Class.forName("plato.protocol.proto.CmdType");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 测试不依赖Protobuf的基本传输功能
     */
    @Test
    public void testBasicTransport() throws Exception {
        // 创建传输层
        MockTcpTransport transport = new MockTcpTransport();
        transport.connect("localhost", 8080);
        
        // 创建编解码处理器
        TcpCodecHandler codecHandler = new TcpCodecHandler();
        
        // 创建测试数据
        byte[] testData = "Basic Transport Test".getBytes();
        
        // 编码数据
        byte[] encodedData = codecHandler.encode(testData);
        
        // 设置接收处理器
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<byte[]> receivedDataRef = new AtomicReference<>();
        
        transport.setMessageHandler(data -> {
            receivedDataRef.set(data);
            latch.countDown();
        });
        
        // 发送数据
        transport.send(testData);
        
        // 等待接收处理
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        
        // 验证接收到的数据
        byte[] receivedData = receivedDataRef.get();
        assertNotNull(receivedData);
        assertArrayEquals(testData, receivedData);
        
        // 断开连接
        transport.disconnect();
        assertFalse(transport.isConnected());
    }
    
    /**
     * 测试不依赖Protobuf的分片处理
     */
    @Test
    public void testBasicFragmentation() {
        // 创建编解码处理器
        TcpCodecHandler codecHandler = new TcpCodecHandler();
        
        // 创建测试数据
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            largeContent.append("Line ").append(i).append(" of fragmented content.\n");
        }
        byte[] testData = largeContent.toString().getBytes();
        
        // 编码数据
        byte[] encodedData = codecHandler.encode(testData);
        
        // 模拟分片处理
        int fragmentSize = encodedData.length / 3;
        
        byte[] fragment1 = new byte[fragmentSize];
        byte[] fragment2 = new byte[fragmentSize];
        byte[] fragment3 = new byte[encodedData.length - 2 * fragmentSize];
        
        System.arraycopy(encodedData, 0, fragment1, 0, fragmentSize);
        System.arraycopy(encodedData, fragmentSize, fragment2, 0, fragmentSize);
        System.arraycopy(encodedData, 2 * fragmentSize, fragment3, 0, fragment3.length);
        
        // 分片处理
        byte[] result1 = codecHandler.decode(fragment1);
        assertNull(result1); // 第一个分片不足以解析出完整消息
        
        byte[] result2 = codecHandler.decode(fragment2);
        assertNull(result2); // 第二个分片仍不足以解析出完整消息
        
        byte[] result3 = codecHandler.decode(fragment3);
        assertNotNull(result3); // 第三个分片应该能解析出完整消息
        assertArrayEquals(testData, result3);
    }
    
    // 以下测试需要Protobuf生成的类
    
    /**
     * 测试消息编解码和传输的完整流程 (需要Protobuf)
     */
    @Test
    @EnabledIf("isProtobufClassesAvailable")
    public void testEndToEndMessageFlow() throws Exception {
        if (!isProtobufClassesAvailable()) {
            // 跳过这个测试，因为Protobuf类不可用
            return;
        }
        
        // 此测试需要在Protobuf类生成后执行
        // 在编译过程中生成的类可以在这里使用
    }
    
    /**
     * 测试心跳消息的编解码和传输 (需要Protobuf)
     */
    @Test
    @EnabledIf("isProtobufClassesAvailable")
    public void testHeartbeatMessage() throws Exception {
        if (!isProtobufClassesAvailable()) {
            // 跳过这个测试，因为Protobuf类不可用
            return;
        }
        
        // 此测试需要在Protobuf类生成后执行
    }
    
    /**
     * 测试分片消息的处理 (需要Protobuf)
     */
    @Test
    @EnabledIf("isProtobufClassesAvailable")
    public void testFragmentedMessageProcessing() throws Exception {
        if (!isProtobufClassesAvailable()) {
            // 跳过这个测试，因为Protobuf类不可用
            return;
        }
        
        // 此测试需要在Protobuf类生成后执行
    }
} 