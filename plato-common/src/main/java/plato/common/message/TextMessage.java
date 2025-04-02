package plato.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文本消息
 * <p>
 * 表示包含文本内容的消息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TextMessage extends AbstractMessage {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 文本内容
     */
    private String content;
    
    /**
     * 默认构造函数
     */
    public TextMessage() {
        super();
    }
    
    /**
     * 构造函数
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 文本内容
     */
    public TextMessage(String senderId, String receiverId, String content) {
        super(senderId, receiverId);
        this.content = content;
    }
    
    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    @Override
    public MessageType getType() {
        return MessageType.TEXT;
    }
    
    /**
     * 克隆消息
     *
     * @return 消息的深拷贝
     */
    @Override
    public Message clone() {
        TextMessage clone = new TextMessage();
        clone.setMessageId(this.getMessageId());
        clone.setTimestamp(this.getTimestamp());
        clone.setSenderId(this.getSenderId());
        clone.setReceiverId(this.getReceiverId());
        clone.setContent(this.getContent());
        return clone;
    }
    
    /**
     * 验证消息的有效性
     *
     * @return 如果消息有效返回true，否则返回false
     */
    @Override
    public boolean isValid() {
        return super.isValid() && content != null && !content.isEmpty();
    }
    
    /**
     * 重写toString方法
     *
     * @return 消息的字符串表示
     */
    @Override
    public String toString() {
        return "TextMessage{" +
                "type=" + getType() +
                ", messageId='" + getMessageId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", senderId='" + getSenderId() + '\'' +
                ", receiverId='" + getReceiverId() + '\'' +
                ", content='" + (content != null && content.length() > 50 ? content.substring(0, 47) + "..." : content) + '\'' +
                '}';
    }
} 