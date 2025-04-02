package plato.cache.infrastructure.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import plato.cache.domain.valueobject.CacheOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存实现
 * 使用Caffeine实现高性能的本地缓存
 * 对应Go项目中的localCache结构体
 */
@Slf4j
public class LocalCache implements plato.cache.domain.service.Cache {
    
    /**
     * Caffeine缓存实例
     */
    private final Cache<String, Object> cache;
    
    /**
     * 构造函数
     * 根据缓存选项创建本地缓存实例
     * 
     * @param options 缓存选项
     */
    public LocalCache(CacheOptions options) {
        log.info("初始化本地缓存，配置：{}", options);
        
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        
        // 设置最大缓存条目数
        if (options.getMaxSize() != null) {
            builder.maximumSize(options.getMaxSize());
        } else {
            // 默认10000条
            builder.maximumSize(10000);
        }
        
        // 设置过期时间
        if (options.getExpireTime() != null) {
            builder.expireAfterWrite(options.getExpireTime().toMillis(), TimeUnit.MILLISECONDS);
        }
        
        this.cache = builder.build();
    }
    
    @Override
    public void mSet(Map<String, Object> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        log.debug("本地缓存批量设置，键值对数量：{}", keys.size());
        keys.forEach(cache::put);
    }
    
    @Override
    public Map<String, Object> mGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        
        log.debug("本地缓存批量获取，键数量：{}", keys.size());
        Map<String, Object> result = new HashMap<>(keys.size());
        Map<String, Object> allPresent = cache.getAllPresent(keys);
        
        keys.forEach(key -> {
            Object value = allPresent.get(key);
            if (value != null) {
                result.put(key, value);
            }
        });
        
        return result;
    }
    
    @Override
    public void mDel(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        log.debug("本地缓存批量删除，键数量：{}", keys.size());
        cache.invalidateAll(keys);
    }
} 