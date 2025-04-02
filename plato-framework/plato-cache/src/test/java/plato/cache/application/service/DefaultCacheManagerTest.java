package plato.cache.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import plato.cache.domain.service.Cache;
import plato.cache.domain.valueobject.CacheMode;
import plato.cache.domain.valueobject.CacheOptions;
import plato.cache.infrastructure.local.LocalCache;
import plato.cache.infrastructure.redis.RedisCache;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 缓存管理器单元测试
 */
class DefaultCacheManagerTest {

    private DefaultCacheManager cacheManager;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private LocalCache localCache;
    
    @Mock
    private RedisCache redisCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 创建缓存管理器
        cacheManager = new DefaultCacheManager();
        
        // 注入RedisTemplate
        ReflectionTestUtils.setField(cacheManager, "redisTemplate", redisTemplate);
    }

    /**
     * 测试添加本地缓存
     */
    @Test
    void testAddLocalCache() {
        // 创建本地缓存选项
        CacheOptions options = CacheOptions.builder()
                .mode(CacheMode.LOCAL)
                .maxSize(100)
                .expireTime(Duration.ofSeconds(5))
                .build();
        
        // 添加本地缓存
        cacheManager.addCache(options);
        
        // 验证缓存已添加
        Cache cache = cacheManager.getCache(CacheMode.LOCAL);
        assertNotNull(cache);
        assertTrue(cache instanceof LocalCache);
    }
    
    /**
     * 测试添加Redis缓存
     */
    @Test
    void testAddRedisCache() {
        // 创建Redis缓存选项
        CacheOptions options = CacheOptions.builder()
                .mode(CacheMode.REMOTE)
                .keyPrefix("test")
                .expireTime(Duration.ofMinutes(10))
                .build();
        
        // 添加Redis缓存
        cacheManager.addCache(options);
        
        // 验证缓存已添加
        Cache cache = cacheManager.getCache(CacheMode.REMOTE);
        assertNotNull(cache);
        assertTrue(cache instanceof RedisCache);
    }
    
    /**
     * 测试多级缓存获取
     */
    @Test
    void testMultiLevelCacheGet() {
        // 准备测试环境
        Map<CacheMode, Cache> cacheMap = new HashMap<>();
        cacheMap.put(CacheMode.LOCAL, localCache);
        cacheMap.put(CacheMode.REMOTE, redisCache);
        
        ReflectionTestUtils.setField(cacheManager, "cacheMap", cacheMap);
        
        // 模拟本地缓存命中部分键
        Map<String, Object> localResult = new HashMap<>();
        localResult.put("key1", "localValue1");
        when(localCache.mGet(any())).thenReturn(localResult);
        
        // 模拟远程缓存命中其他键
        Map<String, Object> remoteResult = new HashMap<>();
        remoteResult.put("key2", "remoteValue2");
        when(redisCache.mGet(any())).thenReturn(remoteResult);
        
        // 测试多级缓存获取
        Map<String, Object> result = cacheManager.mGet(Arrays.asList("key1", "key2", "key3"));
        
        // 验证结果
        assertEquals(2, result.size());
        assertEquals("localValue1", result.get("key1"));
        assertEquals("remoteValue2", result.get("key2"));
        
        // 验证本地缓存回写
        verify(localCache).mSet(remoteResult);
    }
    
    /**
     * 测试多级缓存设置
     */
    @Test
    void testMultiLevelCacheSet() {
        // 准备测试环境
        Map<CacheMode, Cache> cacheMap = new HashMap<>();
        cacheMap.put(CacheMode.LOCAL, localCache);
        cacheMap.put(CacheMode.REMOTE, redisCache);
        
        ReflectionTestUtils.setField(cacheManager, "cacheMap", cacheMap);
        
        // 测试数据
        Map<String, Object> testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", "value2");
        
        // 测试多级缓存设置
        cacheManager.mSet(testData);
        
        // 验证本地缓存和远程缓存都被设置
        verify(localCache).mSet(testData);
        verify(redisCache).mSet(testData);
    }
    
    /**
     * 测试多级缓存删除
     */
    @Test
    void testMultiLevelCacheDel() {
        // 准备测试环境
        Map<CacheMode, Cache> cacheMap = new HashMap<>();
        cacheMap.put(CacheMode.LOCAL, localCache);
        cacheMap.put(CacheMode.REMOTE, redisCache);
        
        ReflectionTestUtils.setField(cacheManager, "cacheMap", cacheMap);
        
        // 测试数据
        java.util.List<String> keys = Arrays.asList("key1", "key2");
        
        // 测试多级缓存删除
        cacheManager.mDel(keys);
        
        // 验证本地缓存和远程缓存都被删除
        verify(localCache).mDel(keys);
        verify(redisCache).mDel(keys);
    }
} 