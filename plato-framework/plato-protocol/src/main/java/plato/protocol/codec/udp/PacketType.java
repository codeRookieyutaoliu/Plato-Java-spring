package plato.protocol.codec.udp;

/**
 * UDP包类型枚举
 * <p>
 * 用于区分不同类型的UDP包，包括可靠和不可靠传输
 * 参考自zfoo项目的UDP实现
 * </p>
 */
public enum PacketType {
    /**
     * 不可靠消息，无需确认接收
     * 适用于高频、可丢失的消息，如位置同步
     */
    UNRELIABLE((byte) 0),
    
    /**
     * 可靠消息，需要确认接收
     * 适用于重要的消息，如聊天、状态变更等
     */
    RELIABLE((byte) 1),
    
    /**
     * 消息确认包，用于确认接收到可靠消息
     */
    ACK((byte) 2),
    
    /**
     * 心跳包，用于保持连接活跃
     */
    HEARTBEAT((byte) 3),
    
    /**
     * 握手包，用于建立连接
     */
    HANDSHAKE((byte) 4),
    
    /**
     * 握手确认包，用于确认连接建立
     */
    HANDSHAKE_ACK((byte) 5);
    
    /**
     * 包类型码
     */
    private final byte code;
    
    /**
     * 构造函数
     *
     * @param code 类型码
     */
    PacketType(byte code) {
        this.code = code;
    }
    
    /**
     * 获取类型码
     *
     * @return 类型码
     */
    public byte getCode() {
        return code;
    }
    
    /**
     * 根据类型码获取包类型
     *
     * @param code 类型码
     * @return 包类型枚举
     * @throws IllegalArgumentException 如果类型码无效
     */
    public static PacketType fromCode(byte code) {
        for (PacketType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的包类型码: " + code);
    }
    
    /**
     * 判断是否是可靠传输类型
     *
     * @return 是否是可靠传输类型
     */
    public boolean isReliable() {
        return this == RELIABLE || this == HANDSHAKE || this == HANDSHAKE_ACK;
    }
    
    /**
     * 判断是否需要确认接收
     *
     * @return 是否需要确认接收
     */
    public boolean requiresAck() {
        return this == RELIABLE || this == HANDSHAKE;
    }
} 