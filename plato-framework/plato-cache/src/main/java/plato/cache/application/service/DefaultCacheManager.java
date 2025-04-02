package plato.cache.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import plato.cache.domain.service.Cache;
import plato.cache.domain.service.CacheManager;
import plato.cache.domain.valueobject.CacheMode;
import plato.cache.domain.valueobject.CacheOptions;
import plato.cache.infrastructure.local.LocalCache;
import plato.cache.infrastructure.redis.RedisCache;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认缓存管理器实现
 * 实现多级缓存管理，支持本地缓存和Redis缓存
 * 对应Go项目中的Manager结构体实现
 */
@Slf4j
@Service
public class DefaultCacheManager implements CacheManager {
    
    /**
     * 缓存选项列表
     */
    private final List<CacheOptions> options;
    
    /**
     * 缓存实现映射
     * 键为缓存模式，值为对应的缓存实现
     */
    private final Map<CacheMode, Cache> cacheMap;
    
    /**
     * Redis模板
     */
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 构造函数
     */
    public DefaultCacheManager() {
        this.options = new ArrayList<>();
        this.cacheMap = new EnumMap<>(CacheMode.class);
        log.info("初始化默认缓存管理器");
    }
    
    @Override
    public CacheManager addCache(CacheOptions options) {
        this.options.add(options);
        
        switch (options.getMode()) {
            case LOCAL:
                cacheMap.put(CacheMode.LOCAL, new LocalCache(options));
                log.info("添加本地缓存实现");
                break;
            case REMOTE:
                if (redisTemplate == null) {
                    log.warn("Redis模板未配置，无法创建Redis缓存");
                } else {
                    cacheMap.put(CacheMode.REMOTE, new RedisCache(redisTemplate, options));
                    log.info("添加Redis缓存实现");
                }
                break;
            default:
                log.warn("未知的缓存模式：{}", options.getMode());
        }
        
        return this;
    }
    
    @Override
    public void mSet(Map<String, Object> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        log.debug("缓存管理器批量设置，键值对数量：{}", keys.size());
        
        // 依次调用各个缓存实现的mSet方法
        cacheMap.values().forEach(cache -> cache.mSet(keys));
    }
    
    @Override
    public Map<String, Object> mGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        
        log.debug("缓存管理器批量获取，键数量：{}", keys.size());
        
        // 先尝试从本地缓存获取
        Map<String, Object> result = new ConcurrentHashMap<>(keys.size());
        List<String> missKeys = new ArrayList<>(keys.size());
        
        Cache localCache = cacheMap.get(CacheMode.LOCAL);
        if (localCache != null) {
            Map<String, Object> localResult = localCache.mGet(keys);
            result.putAll(localResult);
            
            // 记录未命中的键
            keys.forEach(key -> {
                if (!localResult.containsKey(key)) {
                    missKeys.add(key);
                }
            });
        } else {
            missKeys.addAll(keys);
        }
        
        // 如果有未命中的键，从远程缓存获取
        if (!missKeys.isEmpty() && cacheMap.containsKey(CacheMode.REMOTE)) {
            Map<String, Object> remoteResult = cacheMap.get(CacheMode.REMOTE).mGet(missKeys);
            result.putAll(remoteResult);
            
            // 将远程缓存的结果回写到本地缓存
            if (localCache != null && !remoteResult.isEmpty()) {
                localCache.mSet(remoteResult);
            }
        }
        
        return result;
    }
    
    @Override
    public void mDel(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        log.debug("缓存管理器批量删除，键数量：{}", keys.size());
        
        // 依次调用各个缓存实现的mDel方法
        cacheMap.values().forEach(cache -> cache.mDel(keys));
    }
    
    @Override
    public Cache getCache(CacheMode mode) {
        return cacheMap.get(mode);
    }
} 