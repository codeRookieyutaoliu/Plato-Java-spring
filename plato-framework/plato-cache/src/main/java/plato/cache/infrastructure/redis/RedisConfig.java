package plato.cache.infrastructure.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import plato.common.config.PlatoProperties;

/**
 * Redis配置类
 * 负责初始化Redis连接和配置Redis模板
 * 对应Go项目中redis.go中的初始化功能
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "plato.cache.redis", name = "enabled", havingValue = "true")
public class RedisConfig {

    @Autowired(required = false)
    private PlatoProperties platoProperties;

    /**
     * 创建Redis连接工厂
     * 对应Go项目中的InitRedis方法
     *
     * @return Redis连接工厂
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("初始化Redis连接工厂");
        
        String host = "localhost";
        int port = 6379;
        
        if (platoProperties != null && platoProperties.getCache() != null && 
                platoProperties.getCache().getRedis() != null) {
            host = platoProperties.getCache().getRedis().getHost();
            port = platoProperties.getCache().getRedis().getPort();
        }
        
        log.info("Redis配置: host={}, port={}", host, port);
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        return new JedisConnectionFactory(config);
    }
    
    /**
     * 创建Redis操作模板
     *
     * @param redisConnectionFactory Redis连接工厂
     * @return Redis模板
     */
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("初始化Redis操作模板");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        
        // 设置序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 创建字符串Redis操作模板
     *
     * @param redisConnectionFactory Redis连接工厂
     * @return 字符串Redis模板
     */
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("初始化字符串Redis操作模板");
        return new StringRedisTemplate(redisConnectionFactory);
    }
} 