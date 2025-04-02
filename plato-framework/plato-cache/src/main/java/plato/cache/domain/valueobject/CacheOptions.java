package plato.cache.domain.valueobject;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 缓存选项值对象
 * 包含缓存的配置参数
 * 对应Go项目中的Options结构体
 */
@Data
@Builder
public class CacheOptions {
    
    /**
     * 缓存模式
     * 对应Go项目中的Mode字段
     */
    private CacheMode mode;
    
    /**
     * 缓存过期时间
     * Go项目中未明确定义，Java实现中增加的配置项
     */
    private Duration expireTime;
    
    /**
     * 最大缓存条目数
     * 仅对本地缓存有效
     * Go项目中未明确定义，Java实现中增加的配置项
     */
    private Integer maxSize;
    
    /**
     * Redis键前缀
     * 仅对远程缓存有效
     * Go项目中未明确定义，Java实现中增加的配置项
     */
    private String keyPrefix;
} 