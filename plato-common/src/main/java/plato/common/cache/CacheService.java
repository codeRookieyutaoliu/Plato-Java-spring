package plato.common.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存服务接口
 * 定义缓存操作的基本方法
 */
public interface CacheService {
    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 设置缓存并设置过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     */
    void set(String key, Object value, Duration timeout);

    /**
     * 获取缓存
     * @param key 缓存键
     * @param clazz 返回值类型
     * @param <T> 返回值泛型
     * @return 缓存值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     * @param key 缓存键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 批量删除缓存
     * @param keys 缓存键集合
     * @return 删除的数量
     */
    long delete(List<String> keys);

    /**
     * 设置过期时间
     * @param key 缓存键
     * @param timeout 过期时间
     * @return 是否设置成功
     */
    boolean expire(String key, Duration timeout);

    /**
     * 获取过期时间
     * @param key 缓存键
     * @return 过期时间（秒）
     */
    long getExpire(String key);

    /**
     * 判断缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    boolean hasKey(String key);

    /**
     * 递增
     * @param key 缓存键
     * @param delta 递增因子
     * @return 递增后的值
     */
    long incr(String key, long delta);

    /**
     * 递减
     * @param key 缓存键
     * @param delta 递减因子
     * @return 递减后的值
     */
    long decr(String key, long delta);

    /**
     * 获取Hash结构中的属性
     * @param key 缓存键
     * @param hashKey Hash键
     * @param clazz 返回值类型
     * @param <T> 返回值泛型
     * @return Hash值
     */
    <T> T hGet(String key, String hashKey, Class<T> clazz);

    /**
     * 向Hash结构中放入一个属性
     * @param key 缓存键
     * @param hashKey Hash键
     * @param value Hash值
     */
    void hSet(String key, String hashKey, Object value);

    /**
     * 向Hash结构中放入一个属性并设置过期时间
     * @param key 缓存键
     * @param hashKey Hash键
     * @param value Hash值
     * @param timeout 过期时间
     */
    void hSet(String key, String hashKey, Object value, Duration timeout);

    /**
     * 获取Hash结构中的所有属性
     * @param key 缓存键
     * @return Hash结构
     */
    Map<Object, Object> hGetAll(String key);

    /**
     * 删除Hash结构中的属性
     * @param key 缓存键
     * @param hashKeys Hash键集合
     * @return 删除的数量
     */
    long hDelete(String key, Object... hashKeys);

    /**
     * 判断Hash结构中是否有该属性
     * @param key 缓存键
     * @param hashKey Hash键
     * @return 是否存在
     */
    boolean hHasKey(String key, String hashKey);

    /**
     * 向Set结构中添加属性
     * @param key 缓存键
     * @param values 值集合
     * @return 添加的数量
     */
    long sAdd(String key, Object... values);

    /**
     * 向Set结构中添加属性并设置过期时间
     * @param key 缓存键
     * @param timeout 过期时间
     * @param values 值集合
     * @return 添加的数量
     */
    long sAdd(String key, Duration timeout, Object... values);

    /**
     * 获取Set结构
     * @param key 缓存键
     * @param clazz 返回值类型
     * @param <T> 返回值泛型
     * @return 值集合
     */
    <T> Set<T> sMembers(String key, Class<T> clazz);

    /**
     * 判断Set结构中是否存在该值
     * @param key 缓存键
     * @param value 值
     * @return 是否存在
     */
    boolean sIsMember(String key, Object value);

    /**
     * 获取Set结构的长度
     * @param key 缓存键
     * @return 长度
     */
    long sSize(String key);

    /**
     * 删除Set结构中的值
     * @param key 缓存键
     * @param values 值集合
     * @return 删除的数量
     */
    long sRemove(String key, Object... values);

    /**
     * 向List结构中添加属性
     * @param key 缓存键
     * @param value 值
     * @return 添加后的长度
     */
    long lPush(String key, Object value);

    /**
     * 向List结构中添加属性并设置过期时间
     * @param key 缓存键
     * @param value 值
     * @param timeout 过期时间
     * @return 添加后的长度
     */
    long lPush(String key, Object value, Duration timeout);

    /**
     * 向List结构中批量添加属性
     * @param key 缓存键
     * @param values 值集合
     * @return 添加后的长度
     */
    long lPushAll(String key, Object... values);

    /**
     * 向List结构中批量添加属性并设置过期时间
     * @param key 缓存键
     * @param timeout 过期时间
     * @param values 值集合
     * @return 添加后的长度
     */
    long lPushAll(String key, Duration timeout, Object... values);

    /**
     * 从List结构中获取指定范围的属性
     * @param key 缓存键
     * @param start 开始位置
     * @param end 结束位置
     * @param clazz 返回值类型
     * @param <T> 返回值泛型
     * @return 值集合
     */
    <T> List<T> lRange(String key, long start, long end, Class<T> clazz);

    /**
     * 获取List结构的长度
     * @param key 缓存键
     * @return 长度
     */
    long lSize(String key);

    /**
     * 从List结构中移除属性
     * @param key 缓存键
     * @param count 移除数量
     * @param value 值
     * @return 移除的数量
     */
    long lRemove(String key, long count, Object value);
}