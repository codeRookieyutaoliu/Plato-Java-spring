package plato.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置类
 * 配置应用级别的Bean
 */
@Configuration
public class ApplicationConfig {

    /**
     * 配置ObjectMapper
     * 用于JSON序列化和反序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
} 