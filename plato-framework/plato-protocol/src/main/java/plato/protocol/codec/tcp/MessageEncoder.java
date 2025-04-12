package plato.protocol.codec.tcp;

import java.nio.ByteBuffer;

/**
 * 消息编码器
 * <p>
 * 负责将消息编码为二进制格式，对应Go项目中的消息编码逻辑
 * 参考自: common/tcp/write.go
 * </p>
 */
public class MessageEncoder {
    
    /**
     * 将消息编码为二进制格式
     * <p>
     * 采用长度前缀的帧格式，先写入4字节的长度，再写入消息内容
     * </p>
     *
     * @param message 要编码的消息体
     * @return 编码后的二进制数据
     */
    public static byte[] encode(byte[] message) {
        if (message == null || message.length == 0) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        
        // 创建数据包并序列化
        DataPacket packet = new DataPacket(message);
        return packet.marshal();
    }
    
    /**
     * 将多个消息批量编码为一个二进制数据包
     * <p>
     * 在Go项目中没有对应的方法，这是Java版本的扩展功能
     * </p>
     *
     * @param messages 要编码的多个消息
     * @return 包含所有消息的二进制数据
     */
    public static byte[] encodeBatch(byte[]... messages) {
        if (messages == null || messages.length == 0) {
            throw new IllegalArgumentException("消息列表不能为空");
        }
        
        // 计算总长度
        int totalLength = 0;
        for (byte[] message : messages) {
            if (message != null) {
                // 每个消息需要4字节长度 + 消息本身长度
                totalLength += (4 + message.length);
            }
        }
        
        // 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        
        // 依次写入每个消息
        for (byte[] message : messages) {
            if (message != null) {
                buffer.putInt(message.length);
                buffer.put(message);
            }
        }
        
        return buffer.array();
    }
} 