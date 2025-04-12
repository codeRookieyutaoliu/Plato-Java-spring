package plato.protocol.serializer;

import java.io.IOException;

/**
 * 序列化器接口
 * <p>
 * 定义消息序列化和反序列化的基本操作。
 * 对应Go项目中的编解码接口，但更专注于对象与字节流之间的转换。
 *
 * @param <T> 要序列化/反序列化的对象类型
 */
public interface Serializer<T> {

    /**
     * 将对象序列化为字节数组
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     * @throws IOException 序列化过程中可能发生的异常
     */
    byte[] serialize(T obj) throws IOException;

    /**
     * 将字节数组反序列化为对象
     *
     * @param data 要反序列化的字节数组
     * @return 反序列化后的对象
     * @throws IOException 反序列化过程中可能发生的异常
     */
    T deserialize(byte[] data) throws IOException;

    /**
     * 获取序列化器类型
     * 
     * @return 序列化器类型
     */
    SerializerType getType();
} 