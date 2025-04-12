package plato.protocol.codec.tcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * TCP编解码测试类
 * <p>
 * 用于测试TCP协议的编解码功能是否正常
 * </p>
 */
public class TcpCodecTest {
    
    private TcpCodecHandler codecHandler;
    private DataPacket packet;
    private byte[] testData;
    
    @BeforeEach
    public void setUp() {
        // 创建编解码处理器
        codecHandler = new TcpCodecHandler(1024 * 1024); // 设置1MB的最大帧长度
        
        // 准备测试数据
        testData = "Hello, Plato!".getBytes();
        packet = new DataPacket(testData);
    }
    
    /**
     * 测试数据包的序列化和反序列化
     */
    @Test
    public void testDataPacketMarshalAndUnmarshal() {
        // 序列化
        byte[] marshaledData = packet.marshal();
        
        // 预期长度: 4字节长度头 + 测试数据长度
        assertEquals(4 + testData.length, marshaledData.length);
        
        // 确认长度字段正确
        ByteBuffer buffer = ByteBuffer.wrap(marshaledData);
        int length = buffer.getInt();
        assertEquals(testData.length, length);
        
        // 反序列化
        DataPacket unmarshaledPacket = DataPacket.unmarshal(marshaledData);
        
        // 验证反序列化结果
        assertEquals(testData.length, unmarshaledPacket.getLength());
        assertArrayEquals(testData, unmarshaledPacket.getData());
    }
    
    /**
     * 测试编码功能
     */
    @Test
    public void testEncode() {
        // 使用编码器编码数据
        byte[] encodedData = codecHandler.encode(testData);
        
        // 预期长度: 4字节长度头 + 测试数据长度
        assertEquals(4 + testData.length, encodedData.length);
        
        // 确认内容正确
        ByteBuffer buffer = ByteBuffer.wrap(encodedData);
        int length = buffer.getInt();
        assertEquals(testData.length, length);
        
        byte[] content = new byte[length];
        buffer.get(content);
        assertArrayEquals(testData, content);
    }
    
    /**
     * 测试解码功能
     */
    @Test
    public void testDecode() {
        // 编码数据
        byte[] encodedData = codecHandler.encode(testData);
        
        // 解码数据
        byte[] decodedData = codecHandler.decode(encodedData);
        
        // 验证解码结果
        assertNotNull(decodedData);
        assertArrayEquals(testData, decodedData);
    }
    
    /**
     * 测试处理分片包
     */
    @Test
    public void testFragmentedPackets() {
        // 编码数据
        byte[] encodedData = codecHandler.encode(testData);
        
        // 模拟网络分片：分成两部分发送
        byte[] part1 = new byte[encodedData.length / 2];
        byte[] part2 = new byte[encodedData.length - part1.length];
        
        System.arraycopy(encodedData, 0, part1, 0, part1.length);
        System.arraycopy(encodedData, part1.length, part2, 0, part2.length);
        
        // 第一部分数据不足以解析出一个完整的消息
        byte[] result1 = codecHandler.decode(part1);
        assertNull(result1);
        
        // 第二部分数据到达后，应该能解析出完整消息
        byte[] result2 = codecHandler.decode(part2);
        assertNotNull(result2);
        assertArrayEquals(testData, result2);
    }
    
    /**
     * 测试解码多个消息
     */
    @Test
    public void testDecodeMultiple() {
        // 准备两个不同的测试数据
        byte[] testData1 = "First message".getBytes();
        byte[] testData2 = "Second message".getBytes();
        
        // 编码两个消息
        byte[] encodedData1 = codecHandler.encode(testData1);
        byte[] encodedData2 = codecHandler.encode(testData2);
        
        // 将两个编码后的消息合并成一个字节数组
        byte[] combinedData = new byte[encodedData1.length + encodedData2.length];
        System.arraycopy(encodedData1, 0, combinedData, 0, encodedData1.length);
        System.arraycopy(encodedData2, 0, combinedData, encodedData1.length, encodedData2.length);
        
        // 解码多个消息
        List<byte[]> decodedMessages = codecHandler.decodeMultiple(combinedData);
        
        // 验证解码结果
        assertEquals(2, decodedMessages.size());
        assertArrayEquals(testData1, decodedMessages.get(0));
        assertArrayEquals(testData2, decodedMessages.get(1));
    }
    
    /**
     * 测试无效数据处理
     */
    @Test
    public void testInvalidData() {
        // 空数据
        assertThrows(IllegalArgumentException.class, () -> {
            codecHandler.encode(new byte[0]);
        });
        
        // 长度为负数的无效帧
        ByteBuffer invalidBuffer = ByteBuffer.allocate(8);
        invalidBuffer.putInt(-1); // 无效的长度
        invalidBuffer.putInt(0);  // 填充一些数据
        
        assertThrows(IllegalArgumentException.class, () -> {
            codecHandler.decode(invalidBuffer.array());
        });
    }
} 