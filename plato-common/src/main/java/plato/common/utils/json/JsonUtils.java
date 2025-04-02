package plato.common.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON工具类
 * <p>
 * 基于Jackson库实现的JSON序列化和反序列化工具，提供：
 * - 对象转JSON字符串
 * - JSON字符串转对象
 * - 支持泛型类型的JSON转换
 * - 获取ObjectMapper实例
 * </p>
 * 在Go代码中通常使用标准库的json包实现类似功能
 */
public class JsonUtils {
    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtils() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * 全局共享的ObjectMapper实例
     * 配置了Java 8时间类型支持和日期格式化
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 待转换的对象
     * @return JSON字符串
     * @throws RuntimeException 如果转换失败
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("将对象转换为JSON失败", e);
        }
    }

    /**
     * 将JSON字符串转换为指定类型的对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型Class对象
     * @param <T> 目标类型
     * @return 转换后的对象
     * @throws RuntimeException 如果转换失败
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析JSON到对象失败", e);
        }
    }

    /**
     * 将JSON字符串转换为复杂类型（如泛型集合）的对象
     *
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 目标类型
     * @return 转换后的对象
     * @throws RuntimeException 如果转换失败
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析JSON到复杂类型对象失败", e);
        }
    }

    /**
     * 获取ObjectMapper实例
     * 可用于进行更复杂的JSON操作
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
} 