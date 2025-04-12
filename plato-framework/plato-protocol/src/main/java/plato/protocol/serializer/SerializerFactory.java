package plato.protocol.serializer;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import plato.protocol.serializer.json.JsonSerializer;
import plato.protocol.serializer.protobuf.ProtobufSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器工厂
 * <p>
 * 用于创建和管理不同类型的序列化器。
 * 对应Go项目中的编解码器工厂。
 */
public class SerializerFactory {

    // 缓存已创建的序列化器
    private static final Map<String, Serializer<?>> SERIALIZER_CACHE = new ConcurrentHashMap<>();

    /**
     * 创建 Protobuf 序列化器
     *
     * @param messageClass Protocol Buffers消息类
     * @param parser Protocol Buffers消息解析器
     * @param <T> 消息类型
     * @return Protobuf序列化器
     */
    public static <T extends Message> Serializer<T> createProtobufSerializer(Class<T> messageClass, Parser<T> parser) {
        String key = "protobuf:" + messageClass.getName();
        @SuppressWarnings("unchecked")
        Serializer<T> serializer = (Serializer<T>) SERIALIZER_CACHE.computeIfAbsent(key, k -> new ProtobufSerializer<>(parser));
        return serializer;
    }

    /**
     * 创建 JSON 序列化器
     *
     * @param messageClass Protocol Buffers消息类
     * @param <T> 消息类型
     * @return JSON序列化器
     */
    public static <T extends Message> Serializer<T> createJsonSerializer(Class<T> messageClass) {
        String key = "json:" + messageClass.getName();
        @SuppressWarnings("unchecked")
        Serializer<T> serializer = (Serializer<T>) SERIALIZER_CACHE.computeIfAbsent(key, k -> new JsonSerializer<>(messageClass));
        return serializer;
    }

    /**
     * 创建指定类型的序列化器
     *
     * @param type 序列化器类型
     * @param messageClass Protocol Buffers消息类
     * @param parser Protocol Buffers消息解析器
     * @param <T> 消息类型
     * @return 对应类型的序列化器
     */
    @SuppressWarnings("unchecked")
    public static <T extends Message> Serializer<T> createSerializer(SerializerType type, Class<T> messageClass, Parser<T> parser) {
        switch (type) {
            case PROTOBUF:
                return createProtobufSerializer(messageClass, parser);
            case JSON:
                return createJsonSerializer(messageClass);
            default:
                throw new IllegalArgumentException("Unsupported serializer type: " + type);
        }
    }

    /**
     * 获取对应类型的序列化器（如果已创建）
     *
     * @param type 序列化器类型
     * @param messageClass 消息类
     * @param <T> 消息类型
     * @return 对应的序列化器，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public static <T extends Message> Serializer<T> getSerializer(SerializerType type, Class<T> messageClass) {
        String key = type.name().toLowerCase() + ":" + messageClass.getName();
        return (Serializer<T>) SERIALIZER_CACHE.get(key);
    }

    /**
     * 清除缓存的序列化器
     */
    public static void clearCache() {
        SERIALIZER_CACHE.clear();
    }
} 