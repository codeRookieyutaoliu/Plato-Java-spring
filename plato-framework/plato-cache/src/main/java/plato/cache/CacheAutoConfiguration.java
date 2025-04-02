package plato.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import plato.cache.domain.service.CacheManager;
import plato.cache.domain.valueobject.CacheMode;
import plato.cache.domain.valueobject.CacheOptions;
import plato.cache.application.service.DefaultCacheManager;

import java.time.Duration;

/**
 * 缓存自动配置类
 * 负责初始化缓存相关的Bean
 */
@Slf4j
@Configuration
@ComponentScan("plato.cache")
public class CacheAutoConfiguration {

    /**
     * 创建缓存管理器
     *
     * @return 缓存管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        log.info("自动配置缓存管理器");
        
        DefaultCacheManager cacheManager = new DefaultCacheManager();
        
        // 配置本地缓存
        cacheManager.addCache(CacheOptions.builder()
                .mode(CacheMode.LOCAL)
                .maxSize(10000)
                .expireTime(Duration.ofHours(1))
                .build());
        
        return cacheManager;
    }
} 