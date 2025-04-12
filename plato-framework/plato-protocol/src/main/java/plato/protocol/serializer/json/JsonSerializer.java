package plato.protocol.serializer.json;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import plato.protocol.serializer.SerializationException;
import plato.protocol.serializer.Serializer;
import plato.protocol.serializer.SerializerType;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * JSON序列化器实现
 * <p>
 * 用于Protocol Buffers消息的JSON序列化和反序列化。
 * 使用Protocol Buffers自带的JSON工具类进行转换。
 * 对应Go项目中的JSON编解码器。
 *
 * @param <T> Protocol Buffers生成的消息类型
 */
public class JsonSerializer<T extends Message> implements Serializer<T> {

    private final Class<T> clazz;
    private final JsonFormat.Printer printer;
    private final JsonFormat.Parser parser;

    /**
     * 创建一个JSON序列化器
     *
     * @param clazz Protocol Buffers消息类型
     */
    public JsonSerializer(Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }
        this.clazz = clazz;
        this.printer = JsonFormat.printer().includingDefaultValueFields();
        this.parser = JsonFormat.parser().ignoringUnknownFields();
    }

    @Override
    public byte[] serialize(T obj) throws IOException {
        if (obj == null) {
            throw new SerializationException("Cannot serialize null object");
        }
        
        try {
            String json = printer.print(obj);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SerializationException("Error serializing object to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public T deserialize(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            throw new SerializationException("Cannot deserialize null or empty data");
        }
        
        try {
            // 获取newBuilder静态方法
            Method newBuilderMethod = clazz.getMethod("newBuilder");
            Message.Builder builder = (Message.Builder) newBuilderMethod.invoke(null);
            
            // 解析JSON
            parser.merge(new String(data, StandardCharsets.UTF_8), builder);
            
            // 构建消息对象
            @SuppressWarnings("unchecked")
            T message = (T) builder.build();
            return message;
        } catch (Exception e) {
            throw new SerializationException("Error deserializing JSON to object: " + e.getMessage(), e);
        }
    }

    @Override
    public SerializerType getType() {
        return SerializerType.JSON;
    }
} 