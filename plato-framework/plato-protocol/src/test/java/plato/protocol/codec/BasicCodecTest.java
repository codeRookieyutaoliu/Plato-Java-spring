package plato.protocol.codec;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import plato.protocol.codec.tcp.DataPacket;
import plato.protocol.codec.tcp.MessageEncoder;
import plato.protocol.codec.tcp.MessageDecoder;
import plato.protocol.codec.tcp.TcpCodecHandler;
import java.nio.ByteBuffer;

/**
 * 基本编解码测试类
 * <p>
 * 不依赖于protobuf生成的类，测试基本的编解码功能
 * </p>
 */
public class BasicCodecTest {
    
    /**
     * 测试DataPacket的序列化和反序列化
     */
    @Test
    public void testDataPacket() {
        // 创建测试数据
        byte[] testData = "Hello, Plato!".getBytes();
        
        // 创建数据包
        DataPacket packet = new DataPacket(testData);
        
        // 验证数据包属性
        assertEquals(testData.length, packet.getLength());
        assertArrayEquals(testData, packet.getData());
        
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
     * 测试消息编码和解码
     */
    @Test
    public void testMessageEncoderAndDecoder() {
        // 创建测试数据
        byte[] testData = "Test Message".getBytes();
        
        // 编码
        byte[] encodedData = MessageEncoder.encode(testData);
        
        // 解码
        byte[] decodedData = MessageDecoder.decode(encodedData);
        
        // 验证编解码结果
        assertArrayEquals(testData, decodedData);
    }
    
    /**
     * 测试TCP编解码处理器
     */
    @Test
    public void testTcpCodecHandler() {
        // 创建编解码处理器
        TcpCodecHandler codecHandler = new TcpCodecHandler();
        
        // 创建测试数据
        byte[] testData = "TCP Codec Test".getBytes();
        
        // 编码
        byte[] encodedData = codecHandler.encode(testData);
        
        // 解码
        byte[] decodedData = codecHandler.decode(encodedData);
        
        // 验证编解码结果
        assertArrayEquals(testData, decodedData);
    }
    
    /**
     * 测试TCP分片
     */
    @Test
    public void testTcpFragmentation() {
        // 创建编解码处理器
        TcpCodecHandler codecHandler = new TcpCodecHandler();
        
        // 创建测试数据
        byte[] testData = "Fragmentation Test".getBytes();
        
        // 编码
        byte[] encodedData = codecHandler.encode(testData);
        
        // 模拟分片：分成两个部分
        int part1Length = encodedData.length / 2;
        byte[] part1 = new byte[part1Length];
        byte[] part2 = new byte[encodedData.length - part1Length];
        
        System.arraycopy(encodedData, 0, part1, 0, part1Length);
        System.arraycopy(encodedData, part1Length, part2, 0, part2.length);
        
        // 处理第一个分片
        byte[] result1 = codecHandler.decode(part1);
        assertNull(result1); // 第一个分片不足以构成完整消息
        
        // 处理第二个分片
        byte[] result2 = codecHandler.decode(part2);
        
        // 验证解码结果
        assertArrayEquals(testData, result2);
    }
    
    /**
     * 测试多条消息的编解码
     */
    @Test
    public void testMultipleMessages() {
        // 创建编解码处理器
        TcpCodecHandler codecHandler = new TcpCodecHandler();
        
        // 创建测试数据
        byte[] message1 = "First Message".getBytes();
        byte[] message2 = "Second Message".getBytes();
        
        // 编码
        byte[] encoded1 = codecHandler.encode(message1);
        byte[] encoded2 = codecHandler.encode(message2);
        
        // 合并编码后的消息
        byte[] combinedData = new byte[encoded1.length + encoded2.length];
        System.arraycopy(encoded1, 0, combinedData, 0, encoded1.length);
        System.arraycopy(encoded2, 0, combinedData, encoded1.length, encoded2.length);
        
        // 解码多条消息
        codecHandler.clearDecodeBuffer(); // 清空解码缓冲区，确保测试环境干净
        
        // 一次性解码所有消息
        byte[] firstResult = codecHandler.decode(combinedData);
        assertNotNull(firstResult);
        assertArrayEquals(message1, firstResult);
        
        // 从缓冲区获取第二条消息
        byte[] secondResult = codecHandler.decode(new byte[0]);
        assertNotNull(secondResult);
        assertArrayEquals(message2, secondResult);
        
        // 没有更多消息了
        byte[] thirdResult = codecHandler.decode(new byte[0]);
        assertNull(thirdResult);
    }
} 