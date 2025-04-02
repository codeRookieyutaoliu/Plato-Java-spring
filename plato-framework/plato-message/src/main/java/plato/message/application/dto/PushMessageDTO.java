package plato.message.application.dto;

import plato.message.domain.valueobject.MessageType;

/**
 * 推送消息DTO
 * <p>
 * 包含推送消息的所有数据
 * </p>
 */
public class PushMessageDTO extends MessageDTO {
    
    /**
     * 序列化版本UID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息ID
     */
    private final long msgId;
    
    /**
     * 会话ID
     */
    private final long sessionId;
    
    /**
     * 消息内容
     */
    private final byte[] content;
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 消息时间戳
     * @param type 消息类型
     * @param msgId 消息ID
     * @param sessionId 会话ID
     * @param content 消息内容
     */
    public PushMessageDTO(String id, long timestamp, MessageType type, long msgId, long sessionId, byte[] content) {
        super(id, timestamp, type);
        this.msgId = msgId;
        this.sessionId = sessionId;
        this.content = content;
    }
    
    /**
     * 获取消息ID
     *
     * @return 消息ID
     */
    public long getMsgId() {
        return msgId;
    }
    
    /**
     * 获取会话ID
     *
     * @return 会话ID
     */
    public long getSessionId() {
        return sessionId;
    }
    
    /**
     * 获取消息内容
     *
     * @return 消息内容
     */
    public byte[] getContent() {
        return content;
    }
    
    @Override
    public String toString() {
        return "PushMessageDTO{" +
                "id='" + getId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", type=" + getType() +
                ", msgId=" + msgId +
                ", sessionId=" + sessionId +
                ", content=" + (content != null ? content.length + " bytes" : "null") +
                '}';
    }
}