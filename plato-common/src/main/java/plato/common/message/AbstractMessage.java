package plato.common.message;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 * 抽象消息基类
 * <p>
 * 实现Message接口的通用功能，为具体消息类型提供基础实现
 * </p>
 */
@Data
public abstract class AbstractMessage implements Message {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    protected String messageId;
    
    /**
     * 发送时间戳
     */
    protected long timestamp;
    
    /**
     * 发送者ID
     */
    protected String senderId;
    
    /**
     * 接收者ID
     */
    protected String receiverId;
    
    /**
     * 构造函数
     */
    public AbstractMessage() {
        this.messageId = generateMessageId();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 构造函数
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     */
    public AbstractMessage(String senderId, String receiverId) {
        this();
        this.senderId = senderId;
        this.receiverId = receiverId;
    }
    
    /**
     * 生成消息ID
     *
     * @return 唯一的消息ID
     */
    protected String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 默认的序列化实现，子类可以覆盖提供更高效的实现
     *
     * @return 序列化后的字节数组
     */
    @Override
    public byte[] serialize() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new MessageSerializationException("消息序列化失败", e);
        }
    }
    
    /**
     * 默认验证实现，子类应该覆盖提供具体的验证逻辑
     *
     * @return 如果消息有效返回true，否则返回false
     */
    @Override
    public boolean isValid() {
        return messageId != null && !messageId.isEmpty() && timestamp > 0;
    }
    
    /**
     * 克隆消息的抽象方法，子类需要实现
     *
     * @return 消息的深拷贝
     */
    @Override
    public abstract Message clone();
    
    /**
     * 重写toString方法
     *
     * @return 消息的字符串表示
     */
    @Override
    public String toString() {
        return "Message{" +
                "type=" + getType() +
                ", messageId='" + messageId + '\'' +
                ", timestamp=" + timestamp +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                '}';
    }
    
    /**
     * 重写equals方法
     *
     * @param o 要比较的对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AbstractMessage that = (AbstractMessage) o;
        
        return messageId != null ? messageId.equals(that.messageId) : that.messageId == null;
    }
    
    /**
     * 重写hashCode方法
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return messageId != null ? messageId.hashCode() : 0;
    }
} 