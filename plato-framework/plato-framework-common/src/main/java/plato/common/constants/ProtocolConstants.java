package plato.common.constants;

/**
 * 协议常量
 * <p>
 * 定义协议相关的常量值
 * </p>
 */
public class ProtocolConstants {
    
    /**
     * 协议魔数，用于标识协议
     * <p>
     * "PTO"的ASCII码
     * </p>
     */
    public static final int MAGIC_NUMBER = 0x50544F;
    
    /**
     * 协议版本
     */
    public static final int VERSION = 1;
    
    /**
     * 消息头长度（魔数4 + 版本4 + 类型4 + 消息ID8 + 体长度4 = 24字节）
     */
    public static final int HEADER_LENGTH = 24;
    
    /**
     * 最大消息体长度（10MB）
     */
    public static final int MAX_BODY_LENGTH = 10 * 1024 * 1024;
    
    /**
     * TCP相关常量
     */
    public static class Tcp {
        /**
         * 默认最大帧长度（5MB）
         */
        public static final int DEFAULT_MAX_FRAME_LENGTH = 5 * 1024 * 1024;
        
        /**
         * 长度字段长度（4字节）
         */
        public static final int LENGTH_FIELD_LENGTH = 4;
        
        /**
         * 长度字段偏移（0）
         */
        public static final int LENGTH_FIELD_OFFSET = 0;
        
        /**
         * 读超时时间（毫秒）
         */
        public static final int READ_TIMEOUT_MS = 120000;
    }
    
    /**
     * UDP相关常量
     */
    public static class Udp {
        /**
         * 最大UDP包大小（64KB - 8字节UDP头）
         */
        public static final int MAX_PACKET_SIZE = 65507;
        
        /**
         * 包类型字段长度（1字节）
         */
        public static final int PACKET_TYPE_LENGTH = 1;
        
        /**
         * 序列号字段长度（4字节）
         */
        public static final int SEQUENCE_ID_LENGTH = 4;
        
        /**
         * 长度字段长度（4字节）
         */
        public static final int LENGTH_FIELD_LENGTH = 4;
        
        /**
         * UDP头部长度（类型 + 序列号 + 长度 = 9字节）
         */
        public static final int HEADER_LENGTH = PACKET_TYPE_LENGTH + SEQUENCE_ID_LENGTH + LENGTH_FIELD_LENGTH;
        
        /**
         * 最大有效负载大小
         */
        public static final int MAX_PAYLOAD_SIZE = MAX_PACKET_SIZE - HEADER_LENGTH;
        
        /**
         * 重传超时（毫秒）
         */
        public static final int RETRANSMISSION_TIMEOUT_MS = 500;
        
        /**
         * 最大重传次数
         */
        public static final int MAX_RETRANSMISSION_COUNT = 5;
    }
} 