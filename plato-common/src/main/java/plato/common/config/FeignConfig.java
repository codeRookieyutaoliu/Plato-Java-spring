package plato.common.config;

import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import plato.common.feign.ProtobufDecoder;
import plato.common.feign.ProtobufEncoder;

/**
 * Feign配置类
 * 用于配置Feign客户端
 */
@Configuration
@EnableFeignClients(basePackages = "plato.common.feign")
public class FeignConfig {

    /**
     * 配置Feign日志级别
     * NONE: 不记录日志
     * BASIC: 只记录请求方法、URL、响应状态码和执行时间
     * HEADERS: 记录BASIC级别的基础上，记录请求和响应的header
     * FULL: 记录请求和响应的header、body和元数据
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 配置Feign编码器
     * 支持JSON和Protobuf格式
     */
    @Bean
    public Encoder feignEncoder() {
        HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter();
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        
        // 创建支持Protobuf的编码器
        return new ProtobufFallbackEncoder(new SpringEncoder(objectFactory));
    }

    /**
     * 配置Feign解码器
     * 支持JSON和Protobuf格式
     */
    @Bean
    public Decoder feignDecoder() {
        HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter();
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        
        // 创建支持Protobuf的解码器
        return new ResponseEntityDecoder(new ProtobufFallbackDecoder(new SpringDecoder(objectFactory)));
    }
    
    /**
     * Protobuf优先的编码器
     * 先尝试使用Protobuf编码，如果失败则使用默认编码器
     */
    private static class ProtobufFallbackEncoder implements Encoder {
        private final ProtobufEncoder protobufEncoder = new ProtobufEncoder();
        private final Encoder defaultEncoder;
        
        public ProtobufFallbackEncoder(Encoder defaultEncoder) {
            this.defaultEncoder = defaultEncoder;
        }
        
        @Override
        public void encode(Object object, java.lang.reflect.Type bodyType, feign.RequestTemplate template) throws feign.codec.EncodeException {
            try {
                protobufEncoder.encode(object, bodyType, template);
            } catch (Exception e) {
                defaultEncoder.encode(object, bodyType, template);
            }
        }
    }
    
    /**
     * Protobuf优先的解码器
     * 先尝试使用Protobuf解码，如果失败则使用默认解码器
     */
    private static class ProtobufFallbackDecoder implements Decoder {
        private final ProtobufDecoder protobufDecoder = new ProtobufDecoder();
        private final Decoder defaultDecoder;
        
        public ProtobufFallbackDecoder(Decoder defaultDecoder) {
            this.defaultDecoder = defaultDecoder;
        }
        
        @Override
        public Object decode(feign.Response response, java.lang.reflect.Type type) throws java.io.IOException, feign.FeignException {
            try {
                return protobufDecoder.decode(response, type);
            } catch (Exception e) {
                return defaultDecoder.decode(response, type);
            }
        }
    }
} 