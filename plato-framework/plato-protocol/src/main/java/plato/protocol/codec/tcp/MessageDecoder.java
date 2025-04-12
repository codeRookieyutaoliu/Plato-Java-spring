package plato.protocol.codec.tcp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息解码器
 * <p>
 * 负责将二进制数据解码为消息，对应Go项目中的解码逻辑
 * 参考自: common/tcp/read.go
 * </p>
 */
public class MessageDecoder {
    
    /**
     * 解码单个消息
     * <p>
     * 从二进制数据中解析出一个完整的消息
     * </p>
     *
     * @param data 包含长度前缀的二进制数据
     * @return 解码后的消息内容
     * @throws IllegalArgumentException 如果数据格式不正确
     */
    public static byte[] decode(byte[] data) {
        if (data == null || data.length < 4) {
            throw new IllegalArgumentException("数据格式不正确，长度不足");
        }
        
        // 解析数据包
        DataPacket packet = DataPacket.unmarshal(data);
        return packet.getData();
    }
    
    /**
     * 从字节缓冲区中解码消息
     * <p>
     * 从ByteBuffer中解析一个消息，如果缓冲区中的数据不足则返回null
     * </p>
     *
     * @param buffer 包含消息数据的字节缓冲区
     * @return 解码后的消息内容，如果数据不足则返回null
     */
    public static byte[] decodeFromBuffer(ByteBuffer buffer) {
        // 确保有足够的数据读取长度
        if (buffer.remaining() < 4) {
            return null;
        }
        
        // 标记当前位置，用于在数据不足时回退
        buffer.mark();
        
        // 读取长度
        int length = buffer.getInt();
        
        // 检查长度是否合法
        if (length <= 0) {
            buffer.reset(); // 回退到标记位置
            throw new IllegalArgumentException("消息长度不合法: " + length);
        }
        
        // 检查是否有足够的数据
        if (buffer.remaining() < length) {
            buffer.reset(); // 回退到标记位置
            return null;
        }
        
        // 读取消息内容
        byte[] message = new byte[length];
        buffer.get(message);
        
        return message;
    }
    
    /**
     * 解码包含多个消息的二进制数据
     * <p>
     * 在Go项目中没有对应的方法，这是Java版本的扩展功能
     * </p>
     *
     * @param data 包含多个消息的二进制数据
     * @return 解码后的消息列表
     */
    public static List<byte[]> decodeMultiple(byte[] data) {
        if (data == null || data.length < 4) {
            throw new IllegalArgumentException("数据格式不正确，长度不足");
        }
        
        List<byte[]> messages = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        
        while (buffer.hasRemaining()) {
            byte[] message = decodeFromBuffer(buffer);
            if (message == null) {
                break;
            }
            messages.add(message);
        }
        
        return messages;
    }
} 