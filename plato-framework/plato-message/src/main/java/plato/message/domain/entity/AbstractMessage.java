package plato.message.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import plato.message.domain.valueobject.MessageType;

import java.util.UUID;

/**
 * 抽象消息类
 * <p>
 * 实现消息接口中的通用方法，为具体消息类型提供基础功能
 * </p>
 */
public abstract class AbstractMessage implements Message {
    
    /**
     * 消息ID
     */
    private final String id;
    
    /**
     * 消息时间戳（毫秒）
     */
    private final long timestamp;
    
    /**
     * 构造函数
     */
    protected AbstractMessage() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 构造函数
     * 
     * @param id 消息ID
     * @param timestamp 消息时间戳
     */
    protected AbstractMessage(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取消息类型
     * 每个具体的消息类需要实现此方法指定自己的消息类型
     */
    @Override
    @JsonIgnore
    public abstract MessageType getType();
    
    /**
     * 将消息转换为字节数组
     * 每个具体的消息类需要实现此方法指定自己的序列化方式
     */
    @Override
    public abstract byte[] toBytes();
} 