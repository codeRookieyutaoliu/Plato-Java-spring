package plato.cache.domain.service;

import java.util.List;
import java.util.Map;

/**
 * 缓存服务接口
 * 定义了缓存的基本操作，包括批量设置、批量获取和批量删除
 * 对应Go项目中的cache接口
 */
public interface Cache {
    
    /**
     * 批量设置缓存键值对
     * 对应Go项目中的MSet方法
     *
     * @param keys 键值对映射表
     */
    void mSet(Map<String, Object> keys);
    
    /**
     * 批量获取缓存值
     * 对应Go项目中的MGet方法
     *
     * @param keys 键列表
     * @return 键值对映射表
     */
    Map<String, Object> mGet(List<String> keys);
    
    /**
     * 批量删除缓存
     * 对应Go项目中的MDel方法
     *
     * @param keys 键列表
     */
    void mDel(List<String> keys);
} 