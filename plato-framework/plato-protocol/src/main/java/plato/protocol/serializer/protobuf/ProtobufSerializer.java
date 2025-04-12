package plato.protocol.serializer.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import plato.protocol.serializer.SerializationException;
import plato.protocol.serializer.Serializer;
import plato.protocol.serializer.SerializerType;

import java.io.IOException;

/**
 * Protocol Buffers序列化器实现
 * <p>
 * 用于Protocol Buffers类型消息的序列化和反序列化。
 * 对应Go项目中的proto编解码器。
 *
 * @param <T> Protocol Buffers生成的消息类型
 */
public class ProtobufSerializer<T extends Message> implements Serializer<T> {

    private final Parser<T> parser;

    /**
     * 创建一个Protobuf序列化器
     *
     * @param parser Protocol Buffers消息解析器
     */
    public ProtobufSerializer(Parser<T> parser) {
        if (parser == null) {
            throw new IllegalArgumentException("Parser cannot be null");
        }
        this.parser = parser;
    }

    @Override
    public byte[] serialize(T obj) throws IOException {
        if (obj == null) {
            throw new SerializationException("Cannot serialize null object");
        }
        
        try {
            return obj.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Error serializing Protobuf message: " + e.getMessage(), e);
        }
    }

    @Override
    public T deserialize(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            throw new SerializationException("Cannot deserialize null or empty data");
        }
        
        try {
            return parser.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new SerializationException("Error deserializing Protobuf message: " + e.getMessage(), e);
        }
    }

    @Override
    public SerializerType getType() {
        return SerializerType.PROTOBUF;
    }
} 