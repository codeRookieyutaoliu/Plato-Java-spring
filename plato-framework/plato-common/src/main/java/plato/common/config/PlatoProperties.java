package plato.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Plato系统配置属性
 * 对应Go项目中的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "plato")
public class PlatoProperties {

    /**
     * 缓存配置
     */
    private CacheProperties cache = new CacheProperties();
    
    /**
     * 服务发现配置
     */
    private DiscoveryProperties discovery = new DiscoveryProperties();
    
    /**
     * 网关配置
     */
    private GatewayProperties gateway = new GatewayProperties();
    
    /**
     * 状态服务配置
     */
    private StateProperties state = new StateProperties();
    
    /**
     * IP配置服务配置
     */
    private IpConfProperties ipConf = new IpConfProperties();
    
    /**
     * 全局配置
     */
    private GlobalProperties global = new GlobalProperties();
    
    /**
     * 缓存配置属性
     */
    @Data
    public static class CacheProperties {
        /**
         * Redis缓存配置
         */
        private RedisProperties redis = new RedisProperties();
    }
    
    /**
     * Redis配置属性
     */
    @Data
    public static class RedisProperties {
        /**
         * 是否启用Redis
         */
        private boolean enabled = false;
        
        /**
         * Redis主机地址
         */
        private String host = "localhost";
        
        /**
         * Redis端口
         */
        private int port = 6379;
        
        /**
         * Redis密码
         */
        private String password;
        
        /**
         * Redis数据库索引
         */
        private int database = 0;
        
        /**
         * Redis连接池最大连接数
         */
        private int poolSize = 10000;
    }
    
    /**
     * 服务发现配置属性
     */
    @Data
    public static class DiscoveryProperties {
        /**
         * 服务发现地址列表
         */
        private String[] endpoints = {"localhost:8848"};
        
        /**
         * 连接超时时间(秒)
         */
        private int timeout = 5;
    }
    
    /**
     * 网关配置属性
     */
    @Data
    public static class GatewayProperties {
        /**
         * 网关服务地址
         */
        private String serviceAddr = "0.0.0.0";
        
        /**
         * 网关RPC服务端口
         */
        private int rpcServerPort = 8080;
    }
    
    /**
     * 状态服务配置属性
     */
    @Data
    public static class StateProperties {
        /**
         * 命令通道容量
         */
        private int cmdChannelNum = 1000;
        
        /**
         * 登录槽范围
         */
        private int[] loginSlotRange = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    }
    
    /**
     * IP配置服务配置属性
     */
    @Data
    public static class IpConfProperties {
        /**
         * 服务路径
         */
        private String servicePath = "/api/ipconf";
    }
    
    /**
     * 全局配置属性
     */
    @Data
    public static class GlobalProperties {
        /**
         * 环境
         */
        private String env = "prod";
        
        /**
         * 是否为调试环境
         */
        public boolean isDebug() {
            return "debug".equalsIgnoreCase(env);
        }
    }
} 