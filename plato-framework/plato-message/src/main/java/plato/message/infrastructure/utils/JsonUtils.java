package plato.message.infrastructure.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * JSON工具类
 * <p>
 * 提供JSON序列化和反序列化功能
 * </p>
 */
public class JsonUtils {
    
    /**
     * ObjectMapper实例，用于JSON处理
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    static {
        // 配置ObjectMapper
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    }
    
    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtils() {
    }
    
    /**
     * 将对象序列化为JSON字符串
     *
     * @param object 要序列化的对象
     * @return JSON字符串
     * @throws JsonProcessingException 如果序列化失败
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(object);
    }
    
    /**
     * 将对象序列化为格式化的JSON字符串（用于调试）
     *
     * @param object 要序列化的对象
     * @return 格式化的JSON字符串
     * @throws JsonProcessingException 如果序列化失败
     */
    public static String toPrettyJson(Object object) throws JsonProcessingException {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
    
    /**
     * 将JSON字符串反序列化为指定类型的对象
     *
     * @param json JSON字符串
     * @param valueType 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws JsonProcessingException 如果反序列化失败
     */
    public static <T> T fromJson(String json, Class<T> valueType) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, valueType);
    }
    
    /**
     * 将对象序列化为字节数组
     *
     * @param object 要序列化的对象
     * @return 字节数组
     * @throws JsonProcessingException 如果序列化失败
     */
    public static byte[] toJsonBytes(Object object) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }
    
    /**
     * 将字节数组反序列化为指定类型的对象
     *
     * @param bytes 字节数组
     * @param valueType 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws JsonProcessingException 如果反序列化失败
     */
    public static <T> T fromJsonBytes(byte[] bytes, Class<T> valueType) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(bytes, valueType);
    }
    
    /**
     * 获取ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
} 