package plato.cache.infrastructure.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import plato.cache.domain.service.Cache;
import plato.cache.domain.valueobject.CacheOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存实现
 * 使用Spring Data Redis实现Redis缓存操作
 * 对应Go项目中的redisCache结构体
 */
@Slf4j
public class RedisCache implements Cache {

    /**
     * Redis操作模板
     */
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 缓存配置选项
     */
    private final CacheOptions options;
    
    /**
     * 构造函数
     *
     * @param redisTemplate Redis操作模板
     * @param options 缓存配置选项
     */
    public RedisCache(RedisTemplate<String, Object> redisTemplate, CacheOptions options) {
        this.redisTemplate = redisTemplate;
        this.options = options;
        log.info("初始化Redis缓存，配置：{}", options);
    }
    
    /**
     * 获取带前缀的键
     *
     * @param key 原始键
     * @return 带前缀的键
     */
    private String getKeyWithPrefix(String key) {
        String prefix = options.getKeyPrefix();
        return prefix == null || prefix.isEmpty() ? key : prefix + ":" + key;
    }

    @Override
    public void mSet(Map<String, Object> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        log.debug("Redis缓存批量设置，键值对数量：{}", keys.size());
        
        // 添加前缀并设置过期时间
        keys.forEach((key, value) -> {
            String keyWithPrefix = getKeyWithPrefix(key);
            redisTemplate.opsForValue().set(keyWithPrefix, value);
            
            // 设置过期时间
            if (options.getExpireTime() != null) {
                redisTemplate.expire(keyWithPrefix, options.getExpireTime().toMillis(), TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    public Map<String, Object> mGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        
        log.debug("Redis缓存批量获取，键数量：{}", keys.size());
        
        // 添加前缀
        List<String> keysWithPrefix = new ArrayList<>(keys.size());
        for (String key : keys) {
            keysWithPrefix.add(getKeyWithPrefix(key));
        }
        
        // 批量获取
        List<Object> values = redisTemplate.opsForValue().multiGet(keysWithPrefix);
        
        // 组装结果
        Map<String, Object> result = new HashMap<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            Object value = values != null && i < values.size() ? values.get(i) : null;
            if (value != null) {
                result.put(keys.get(i), value);
            }
        }
        
        return result;
    }

    @Override
    public void mDel(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        log.debug("Redis缓存批量删除，键数量：{}", keys.size());
        
        // 添加前缀
        List<String> keysWithPrefix = new ArrayList<>(keys.size());
        for (String key : keys) {
            keysWithPrefix.add(getKeyWithPrefix(key));
        }
        
        redisTemplate.delete(keysWithPrefix);
    }
    
    /**
     * 执行Lua脚本
     * 对应Go项目中的RunLuaInt方法
     *
     * @param scriptName 脚本名称
     * @param keys 键列表
     * @param args 参数列表
     * @return 执行结果
     */
    public Long executeLuaScript(String scriptName, List<String> keys, Object... args) {
        if (scriptName == null || scriptName.isEmpty()) {
            throw new IllegalArgumentException("脚本名称不能为空");
        }
        
        log.debug("执行Lua脚本：{}，键数量：{}", scriptName, keys != null ? keys.size() : 0);
        
        // 添加前缀
        List<String> keysWithPrefix = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                keysWithPrefix.add(getKeyWithPrefix(key));
            }
        }
        
        // 执行脚本
        String script = LuaScriptRegistry.getScript(scriptName);
        return redisTemplate.execute(
                (connection) -> connection.scriptingCommands().evalSha(
                        LuaScriptRegistry.getSha1(script),
                        keysWithPrefix.size(),
                        keysWithPrefix.stream().map(String::getBytes).toArray(byte[][]::new),
                        args
                ),
                redisTemplate.getValueSerializer()
        );
    }
} 