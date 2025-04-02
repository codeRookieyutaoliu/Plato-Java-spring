package plato.cache.domain.valueobject;

/**
 * 缓存模式枚举
 * 定义了缓存的类型，包括本地缓存和远程缓存
 * 对应Go项目中的mode常量定义
 */
public enum CacheMode {
    
    /**
     * 本地缓存模式
     * 对应Go项目中的Local模式
     */
    LOCAL,
    
    /**
     * 远程缓存模式，通常是Redis
     * 对应Go项目中的Remote模式
     */
    REMOTE
} 