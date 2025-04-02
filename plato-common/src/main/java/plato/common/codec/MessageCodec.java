package plato.common.codec;

import com.google.protobuf.Message;

/**
 * 消息编解码器接口
 * 用于编码和解码消息
 */
public interface MessageCodec {
    
    /**
     * 编码消息
     *
     * @param message 消息对象
     * @return 编码后的字节数组
     */
    byte[] encode(Message message);
    
    /**
     * 解码消息
     *
     * @param data 字节数组
     * @param messageClass 消息类型
     * @param <T> 消息类型
     * @return 解码后的消息对象
     */
    <T extends Message> T decode(byte[] data, Class<T> messageClass);
} 