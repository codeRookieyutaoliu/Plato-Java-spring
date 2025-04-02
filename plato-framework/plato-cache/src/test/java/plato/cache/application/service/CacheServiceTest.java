package plato.cache.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import plato.cache.domain.service.Cache;
import plato.cache.domain.service.CacheManager;
import plato.cache.domain.valueobject.CacheMode;
import plato.cache.infrastructure.redis.RedisCache;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 缓存服务单元测试
 */
class CacheServiceTest {

    private CacheService cacheService;
    
    @Mock
    private CacheManager cacheManager;
    
    @Mock
    private RedisCache redisCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 创建缓存服务
        cacheService = new CacheService(cacheManager);
    }
    
    /**
     * 测试获取字节数组
     */
    @Test
    void testGetBytes() {
        // 准备测试数据
        byte[] testBytes = "test".getBytes();
        Map<String, Object> result = Collections.singletonMap("key", testBytes);
        
        // 模拟缓存管理器
        when(cacheManager.mGet(any())).thenReturn(result);
        
        // 测试获取字节数组
        byte[] bytes = cacheService.getBytes("key");
        
        // 验证结果
        assertArrayEquals(testBytes, bytes);
        
        // 验证调用
        verify(cacheManager).mGet(Collections.singletonList("key"));
    }
    
    /**
     * 测试获取字节数组 - 值类型不匹配
     */
    @Test
    void testGetBytesWithTypeNotMatch() {
        // 模拟缓存管理器返回非字节数组
        Map<String, Object> result = Collections.singletonMap("key", "not-a-byte-array");
        when(cacheManager.mGet(any())).thenReturn(result);
        
        // 测试获取字节数组
        byte[] bytes = cacheService.getBytes("key");
        
        // 验证结果为null（类型不匹配）
        assertNull(bytes);
    }
    
    /**
     * 测试获取字节数组 - 缓存不存在
     */
    @Test
    void testGetBytesNotExists() {
        // 模拟缓存管理器返回空结果
        when(cacheManager.mGet(any())).thenReturn(Collections.emptyMap());
        
        // 测试获取不存在的键
        byte[] bytes = cacheService.getBytes("non-exist-key");
        
        // 验证结果为null
        assertNull(bytes);
    }
    
    /**
     * 测试获取64位整数
     */
    @Test
    void testGetUInt64() {
        // 准备测试数据
        Map<String, Object> result = Collections.singletonMap("key", 123L);
        
        // 模拟缓存管理器
        when(cacheManager.mGet(any())).thenReturn(result);
        
        // 测试获取64位整数
        long value = cacheService.getUInt64("key");
        
        // 验证结果
        assertEquals(123L, value);
    }
    
    /**
     * 测试获取64位整数 - 字符串转换
     */
    @Test
    void testGetUInt64WithStringValue() {
        // 模拟缓存管理器返回字符串数字
        Map<String, Object> result = Collections.singletonMap("key", "456");
        when(cacheManager.mGet(any())).thenReturn(result);
        
        // 测试获取64位整数
        long value = cacheService.getUInt64("key");
        
        // 验证结果
        assertEquals(456L, value);
    }
    
    /**
     * 测试设置字节数组
     */
    @Test
    void testSetBytes() {
        // 准备测试数据
        byte[] testBytes = "test".getBytes();
        
        // 测试设置字节数组
        cacheService.setBytes("key", testBytes);
        
        // 验证调用
        verify(cacheManager).mSet(Collections.singletonMap("key", testBytes));
    }
    
    /**
     * 测试删除缓存
     */
    @Test
    void testDel() {
        // 准备测试数据
        List<String> keys = Arrays.asList("key1", "key2");
        
        // 测试删除缓存
        cacheService.del(keys);
        
        // 验证调用
        verify(cacheManager).mDel(keys);
    }
    
    /**
     * 测试设置和获取字符串
     */
    @Test
    void testSetAndGetString() {
        // 准备测试数据
        String key = "key";
        String value = "value";
        
        // 模拟缓存管理器
        Map<String, Object> result = Collections.singletonMap(key, value);
        when(cacheManager.mGet(any())).thenReturn(result);
        
        // 测试设置字符串
        cacheService.setString(key, value);
        
        // 验证设置调用
        verify(cacheManager).mSet(Collections.singletonMap(key, value));
        
        // 测试获取字符串
        String retrievedValue = cacheService.getString(key);
        
        // 验证结果
        assertEquals(value, retrievedValue);
        
        // 验证获取调用
        verify(cacheManager).mGet(Collections.singletonList(key));
    }
    
    /**
     * 测试执行Lua脚本
     */
    @Test
    void testRunLuaInt() {
        // 准备测试数据
        String scriptName = "testScript";
        List<String> keys = Arrays.asList("key1", "key2");
        Object[] args = new Object[]{"arg1", "arg2"};
        
        // 模拟缓存管理器和Redis缓存
        when(cacheManager.getCache(CacheMode.REMOTE)).thenReturn(redisCache);
        when(redisCache.executeLuaScript(eq(scriptName), eq(keys), any())).thenReturn(1L);
        
        // 测试执行Lua脚本
        int result = cacheService.runLuaInt(scriptName, keys, args);
        
        // 验证结果
        assertEquals(1, result);
        
        // 验证调用
        verify(cacheManager).getCache(CacheMode.REMOTE);
        verify(redisCache).executeLuaScript(eq(scriptName), eq(keys), any());
    }
    
    /**
     * 测试执行Lua脚本 - Redis缓存不可用
     */
    @Test
    void testRunLuaIntWithoutRedisCache() {
        // 模拟缓存管理器返回非Redis缓存
        when(cacheManager.getCache(CacheMode.REMOTE)).thenReturn(null);
        
        // 测试执行Lua脚本
        int result = cacheService.runLuaInt("testScript", Collections.emptyList());
        
        // 验证结果为-1（错误标志）
        assertEquals(-1, result);
    }
} 