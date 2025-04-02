package plato.cache.domain.service;

import plato.cache.domain.valueobject.CacheMode;
import plato.cache.domain.valueobject.CacheOptions;

import java.util.List;
import java.util.Map;

/**
 * 缓存管理器接口
 * 管理多种缓存实现，提供统一的缓存操作接口
 * 对应Go项目中的Manager结构体
 */
public interface CacheManager {

    /**
     * 添加缓存实现
     * Go项目中在构造函数中自动添加，Java实现为更灵活的设计
     * 
     * @param options 缓存选项
     * @return 当前缓存管理器实例，支持链式调用
     */
    CacheManager addCache(CacheOptions options);
    
    /**
     * 批量设置缓存
     * 对应Go项目中的MSet方法
     *
     * @param keys 键值对映射
     */
    void mSet(Map<String, Object> keys);
    
    /**
     * 批量获取缓存
     * 对应Go项目中的MGet方法
     *
     * @param keys 键列表
     * @return 键值对映射
     */
    Map<String, Object> mGet(List<String> keys);
    
    /**
     * 批量删除缓存
     * 对应Go项目中的MDel方法
     *
     * @param keys 键列表
     */
    void mDel(List<String> keys);
    
    /**
     * 获取指定类型的缓存
     * Go项目中未直接提供此方法，Java实现中增加的方法
     *
     * @param mode 缓存模式
     * @return 缓存实现
     */
    Cache getCache(CacheMode mode);
} 