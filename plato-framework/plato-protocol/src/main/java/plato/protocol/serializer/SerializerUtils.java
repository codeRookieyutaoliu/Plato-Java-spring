package plato.protocol.serializer;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;

import java.io.IOException;

/**
 * 序列化器工具类
 * <p>
 * 提供一些便捷方法，用于序列化和反序列化操作。
 * 对应Go项目中的编解码工具函数。
 */
public class SerializerUtils {

    /**
     * 使用Protobuf序列化消息
     *
     * @param message Protocol Buffers消息
     * @param <T> 消息类型
     * @return 序列化后的字节数组
     * @throws SerializationException 序列化过程中发生的异常
     */
    public static <T extends Message> byte[] serializeProtobuf(T message) throws SerializationException {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) message.getClass();
            Parser<T> parser = getParserFromMessage(message);
            Serializer<T> serializer = SerializerFactory.createProtobufSerializer(clazz, parser);
            return serializer.serialize(message);
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize message: " + e.getMessage(), e);
        }
    }

    /**
     * 使用Protobuf反序列化消息
     *
     * @param data 序列化后的字节数组
     * @param parser Protocol Buffers消息解析器
     * @param clazz 消息类
     * @param <T> 消息类型
     * @return 反序列化后的消息对象
     * @throws SerializationException 反序列化过程中发生的异常
     */
    public static <T extends Message> T deserializeProtobuf(byte[] data, Parser<T> parser, Class<T> clazz) throws SerializationException {
        try {
            Serializer<T> serializer = SerializerFactory.createProtobufSerializer(clazz, parser);
            return serializer.deserialize(data);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize message: " + e.getMessage(), e);
        }
    }

    /**
     * 使用JSON序列化消息
     *
     * @param message Protocol Buffers消息
     * @param <T> 消息类型
     * @return 序列化后的字节数组
     * @throws SerializationException 序列化过程中发生的异常
     */
    public static <T extends Message> byte[] serializeJson(T message) throws SerializationException {
        try {
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) message.getClass();
            Serializer<T> serializer = SerializerFactory.createJsonSerializer(clazz);
            return serializer.serialize(message);
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize message to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * 使用JSON反序列化消息
     *
     * @param data 序列化后的字节数组
     * @param clazz 消息类
     * @param <T> 消息类型
     * @return 反序列化后的消息对象
     * @throws SerializationException 反序列化过程中发生的异常
     */
    public static <T extends Message> T deserializeJson(byte[] data, Class<T> clazz) throws SerializationException {
        try {
            Serializer<T> serializer = SerializerFactory.createJsonSerializer(clazz);
            return serializer.deserialize(data);
        } catch (IOException e) {
            throw new SerializationException("Failed to deserialize JSON message: " + e.getMessage(), e);
        }
    }

    /**
     * 将一种序列化格式转换为另一种格式
     *
     * @param data 原始数据
     * @param sourceType 源数据的序列化类型
     * @param targetType 目标序列化类型
     * @param clazz 消息类
     * @param parser 消息解析器
     * @param <T> 消息类型
     * @return 转换后的数据
     * @throws SerializationException 转换过程中发生的异常
     */
    public static <T extends Message> byte[] convert(byte[] data, SerializerType sourceType, SerializerType targetType, 
                                                     Class<T> clazz, Parser<T> parser) throws SerializationException {
        try {
            // 如果源类型和目标类型相同，直接返回
            if (sourceType == targetType) {
                return data;
            }
            
            // 先使用源类型反序列化为对象
            Serializer<T> sourceSerializer = SerializerFactory.createSerializer(sourceType, clazz, parser);
            T message = sourceSerializer.deserialize(data);
            
            // 再使用目标类型序列化
            Serializer<T> targetSerializer = SerializerFactory.createSerializer(targetType, clazz, parser);
            return targetSerializer.serialize(message);
        } catch (IOException e) {
            throw new SerializationException("Failed to convert message format: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从消息对象中获取解析器
     *
     * @param message Protocol Buffers消息
     * @param <T> 消息类型
     * @return 消息解析器
     * @throws SerializationException 如果无法获取解析器
     */
    @SuppressWarnings("unchecked")
    private static <T extends Message> Parser<T> getParserFromMessage(T message) throws SerializationException {
        try {
            // Protocol Buffers生成的消息类一般都有getParserForType或getDefaultInstance().getParserForType()方法
            return (Parser<T>) message.getParserForType();
        } catch (Exception e) {
            throw new SerializationException("Failed to get parser from message: " + e.getMessage(), e);
        }
    }
} 