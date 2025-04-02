package plato.common.constants;

/**
 * 系统常量类
 * <p>
 * 定义系统级别的常量，包括：
 * - 服务名称
 * - 协议版本
 * - 默认超时时间
 * - 服务路径
 * </p>
 * 
 * 对应Go项目中通常在各个模块中定义的常量
 */
public final class SystemConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private SystemConstants() {
        throw new UnsupportedOperationException("常量类不支持实例化");
    }

    /**
     * 服务名称常量
     */
    public static final class ServiceNames {
        /**
         * Gateway服务名称
         */
        public static final String GATEWAY = "plato-gateway";
        
        /**
         * State服务名称
         */
        public static final String STATE = "plato-state";
        
        /**
         * IPConf服务名称
         */
        public static final String IP_CONF = "plato-ipconf";
        
        /**
         * 私有构造函数
         */
        private ServiceNames() {
            throw new UnsupportedOperationException("常量类不支持实例化");
        }
    }
    
    /**
     * 协议相关常量
     */
    public static final class Protocol {
        /**
         * 协议版本
         */
        public static final int VERSION = 1;
        
        /**
         * 协议魔数
         */
        public static final int MAGIC_NUMBER = 0x20230101;
        
        /**
         * 私有构造函数
         */
        private Protocol() {
            throw new UnsupportedOperationException("常量类不支持实例化");
        }
    }
    
    /**
     * 超时相关常量
     */
    public static final class Timeout {
        /**
         * 默认连接超时（毫秒）
         */
        public static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
        
        /**
         * 默认读取超时（毫秒）
         */
        public static final int DEFAULT_READ_TIMEOUT_MS = 5000;
        
        /**
         * 默认写入超时（毫秒）
         */
        public static final int DEFAULT_WRITE_TIMEOUT_MS = 5000;
        
        /**
         * 心跳间隔（毫秒）
         */
        public static final int HEARTBEAT_INTERVAL_MS = 30000;
        
        /**
         * 私有构造函数
         */
        private Timeout() {
            throw new UnsupportedOperationException("常量类不支持实例化");
        }
    }
    
    /**
     * 服务路径常量
     */
    public static final class ServicePaths {
        /**
         * Gateway服务路径
         */
        public static final String GATEWAY_PATH = "/plato/gateway";
        
        /**
         * State服务路径
         */
        public static final String STATE_PATH = "/plato/state";
        
        /**
         * IPConf服务路径
         */
        public static final String IP_CONF_PATH = "/plato/ipconf";
        
        /**
         * 私有构造函数
         */
        private ServicePaths() {
            throw new UnsupportedOperationException("常量类不支持实例化");
        }
    }
} 