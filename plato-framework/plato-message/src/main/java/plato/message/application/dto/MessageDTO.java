package plato.message.application.dto;

import plato.message.domain.valueobject.MessageType;

import java.io.Serializable;

/**
 * 消息数据传输对象基类
 * <p>
 * 定义所有消息DTO的通用属性和方法
 * </p>
 */
public abstract class MessageDTO implements Serializable {
    
    /**
     * 序列化版本UID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    private final String id;
    
    /**
     * 消息时间戳（毫秒）
     */
    private final long timestamp;
    
    /**
     * 消息类型
     */
    private final MessageType type;
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 消息时间戳
     * @param type 消息类型
     */
    protected MessageDTO(String id, long timestamp, MessageType type) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
    }
    
    /**
     * 获取消息ID
     *
     * @return 消息ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取消息时间戳
     *
     * @return 消息时间戳（毫秒）
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    public MessageType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return "MessageDTO{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", type=" + type +
                '}';
    }
}