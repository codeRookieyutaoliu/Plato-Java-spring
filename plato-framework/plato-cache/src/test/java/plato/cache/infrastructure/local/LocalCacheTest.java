package plato.cache.infrastructure.local;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import plato.cache.domain.valueobject.CacheMode;
import plato.cache.domain.valueobject.CacheOptions;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 本地缓存单元测试
 */
class LocalCacheTest {

    private LocalCache localCache;

    @BeforeEach
    void setUp() {
        // 创建缓存选项
        CacheOptions options = CacheOptions.builder()
                .mode(CacheMode.LOCAL)
                .maxSize(100)
                .expireTime(Duration.ofSeconds(5))
                .build();
        
        // 创建本地缓存
        localCache = new LocalCache(options);
    }

    /**
     * 测试批量设置和获取缓存
     */
    @Test
    void testMSetAndMGet() {
        // 准备测试数据
        Map<String, Object> testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", 123);
        testData.put("key3", true);
        
        // 设置缓存
        localCache.mSet(testData);
        
        // 获取缓存
        List<String> keys = Arrays.asList("key1", "key2", "key3", "key4");
        Map<String, Object> results = localCache.mGet(keys);
        
        // 验证结果
        assertEquals(3, results.size());
        assertEquals("value1", results.get("key1"));
        assertEquals(123, results.get("key2"));
        assertEquals(true, results.get("key3"));
        assertNull(results.get("key4"));
    }
    
    /**
     * 测试批量删除缓存
     */
    @Test
    void testMDel() {
        // 准备测试数据
        Map<String, Object> testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", "value2");
        testData.put("key3", "value3");
        
        // 设置缓存
        localCache.mSet(testData);
        
        // 删除部分缓存
        localCache.mDel(Arrays.asList("key1", "key3"));
        
        // 获取缓存
        Map<String, Object> results = localCache.mGet(Arrays.asList("key1", "key2", "key3"));
        
        // 验证结果
        assertEquals(1, results.size());
        assertNull(results.get("key1"));
        assertEquals("value2", results.get("key2"));
        assertNull(results.get("key3"));
    }
    
    /**
     * 测试缓存过期
     */
    @Test
    void testExpire() throws InterruptedException {
        // 准备测试数据
        Map<String, Object> testData = new HashMap<>();
        testData.put("expireKey", "expireValue");
        
        // 设置缓存
        localCache.mSet(testData);
        
        // 验证缓存已设置成功
        Map<String, Object> results = localCache.mGet(Arrays.asList("expireKey"));
        assertEquals("expireValue", results.get("expireKey"));
        
        // 等待缓存过期（6秒，比设置的5秒多一点）
        Thread.sleep(6000);
        
        // 验证缓存已过期
        results = localCache.mGet(Arrays.asList("expireKey"));
        assertTrue(results.isEmpty());
    }
    
    /**
     * 测试空参数处理
     */
    @Test
    void testNullAndEmptyInputs() {
        // 测试空Map
        localCache.mSet(new HashMap<>());
        
        // 测试空List
        Map<String, Object> results = localCache.mGet(Arrays.asList());
        assertTrue(results.isEmpty());
        
        // 测试删除空List
        localCache.mDel(Arrays.asList());
        
        // 以上操作应该不会抛出异常
    }
} 