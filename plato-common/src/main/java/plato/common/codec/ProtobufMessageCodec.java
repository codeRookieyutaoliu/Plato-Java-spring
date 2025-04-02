package plato.common.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Protobuf 消息编解码器
 * 用于编码和解码 Protobuf 消息
 */
@Slf4j
@Component
public class ProtobufMessageCodec implements MessageCodec {
    
    /**
     * 编码消息
     *
     * @param message 消息对象
     * @return 编码后的字节数组
     */
    @Override
    public byte[] encode(Message message) {
        return message.toByteArray();
    }
    
    /**
     * 解码消息
     *
     * @param data 字节数组
     * @param messageClass 消息类型
     * @param <T> 消息类型
     * @return 解码后的消息对象
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Message> T decode(byte[] data, Class<T> messageClass) {
        try {
            // 获取 parser 方法
            Method parserMethod = messageClass.getMethod("parser");
            Parser<T> parser = (Parser<T>) parserMethod.invoke(null);
            
            // 解析消息
            return parser.parseFrom(data);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("获取 parser 方法失败: {}", e.getMessage());
            throw new RuntimeException("获取 parser 方法失败", e);
        } catch (InvalidProtocolBufferException e) {
            log.error("解析消息失败: {}", e.getMessage());
            throw new RuntimeException("解析消息失败", e);
        }
    }
} 