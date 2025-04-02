package plato.cache.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plato.cache.domain.service.Cache;
import plato.cache.domain.service.CacheManager;
import plato.cache.domain.valueobject.CacheMode;
import plato.cache.infrastructure.redis.RedisCache;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 缓存服务类
 * 提供常用的缓存操作方法
 */
@Slf4j
@Service
public class CacheService {

    private final CacheManager cacheManager;
    
    @Autowired
    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        log.info("初始化缓存服务");
    }
    
    /**
     * 获取字节数组
     */
    public byte[] getBytes(String key) {
        Map<String, Object> result = cacheManager.mGet(Collections.singletonList(key));
        Object value = result.get(key);
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof byte[]) {
            return (byte[]) value;
        } else {
            log.warn("缓存值类型不是字节数组：{}", key);
            return null;
        }
    }
    
    /**
     * 获取64位整数
     */
    public Long getUInt64(String key) {
        Map<String, Object> result = cacheManager.mGet(Collections.singletonList(key));
        Object value = result.get(key);
        
        if (value == null) {
            return 0L;
        }
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("缓存值不是有效的数字：{}", value);
                return 0L;
            }
        } else {
            log.warn("缓存值类型不是数字：{}", key);
            return 0L;
        }
    }
    
    /**
     * 设置字节数组
     */
    public void setBytes(String key, byte[] value) {
        cacheManager.mSet(Collections.singletonMap(key, value));
    }
    
    /**
     * 删除缓存
     */
    public void del(List<String> keys) {
        cacheManager.mDel(keys);
    }
    
    /**
     * 设置字符串
     */
    public void setString(String key, String value) {
        cacheManager.mSet(Collections.singletonMap(key, value));
    }
    
    /**
     * 获取字符串
     */
    public String getString(String key) {
        Map<String, Object> result = cacheManager.mGet(Collections.singletonList(key));
        Object value = result.get(key);
        
        if (value == null) {
            return null;
        }
        
        if (value instanceof String) {
            return (String) value;
        } else {
            return String.valueOf(value);
        }
    }
    
    /**
     * 执行Lua脚本
     */
    public int runLuaInt(String name, List<String> keys, Object... args) {
        Cache cache = cacheManager.getCache(CacheMode.REMOTE);
        if (cache instanceof RedisCache) {
            Long result = ((RedisCache) cache).executeLuaScript(name, keys, args);
            return result != null ? result.intValue() : -1;
        } else {
            log.warn("没有配置Redis缓存，无法执行Lua脚本");
            return -1;
        }
    }
} 