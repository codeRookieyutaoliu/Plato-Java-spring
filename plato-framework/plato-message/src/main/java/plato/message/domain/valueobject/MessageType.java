package plato.message.domain.valueobject;

/**
 * 消息类型枚举
 * <p>
 * 定义系统中所有消息类型，与Protocol Buffers中的CmdType对应
 * </p>
 * 
 * 对应Go项目中common/idl/message/message.proto中的CmdType枚举
 */
public enum MessageType {
    /**
     * 登录消息
     */
    LOGIN(0),
    
    /**
     * 心跳消息
     */
    HEARTBEAT(1),
    
    /**
     * 重连消息
     */
    RECONNECT(2),
    
    /**
     * 确认消息
     */
    ACK(3),
    
    /**
     * 上行消息
     */
    UP(4),
    
    /**
     * 下行推送消息
     */
    PUSH(5);
    
    /**
     * 消息类型代码
     */
    private final int code;
    
    /**
     * 构造函数
     * 
     * @param code 消息类型代码
     */
    MessageType(int code) {
        this.code = code;
    }
    
    /**
     * 获取消息类型代码
     * 
     * @return 消息类型代码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 根据代码获取消息类型
     * 
     * @param code 消息类型代码
     * @return 消息类型，如果代码无效则返回null
     */
    public static MessageType fromCode(int code) {
        for (MessageType type : MessageType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
} 