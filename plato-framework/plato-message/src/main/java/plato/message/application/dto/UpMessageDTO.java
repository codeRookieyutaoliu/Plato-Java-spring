package plato.message.application.dto;

import plato.message.domain.valueobject.MessageType;

/**
 * 上行消息DTO
 * <p>
 * 包含上行消息的所有数据
 * </p>
 */
public class UpMessageDTO extends MessageDTO {
    
    /**
     * 序列化版本UID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 客户端ID
     */
    private final long clientId;
    
    /**
     * 连接ID
     */
    private final long connId;
    
    /**
     * 会话ID
     */
    private final String sessionId;
    
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
     * @param clientId 客户端ID
     * @param connId 连接ID
     * @param sessionId 会话ID
     * @param content 消息内容
     */
    public UpMessageDTO(String id, long timestamp, MessageType type, long clientId, long connId, String sessionId, byte[] content) {
        super(id, timestamp, type);
        this.clientId = clientId;
        this.connId = connId;
        this.sessionId = sessionId;
        this.content = content;
    }
    
    /**
     * 获取客户端ID
     *
     * @return 客户端ID
     */
    public long getClientId() {
        return clientId;
    }
    
    /**
     * 获取连接ID
     *
     * @return 连接ID
     */
    public long getConnId() {
        return connId;
    }
    
    /**
     * 获取会话ID
     *
     * @return 会话ID
     */
    public String getSessionId() {
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
        return "UpMessageDTO{" +
                "id='" + getId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", type=" + getType() +
                ", clientId=" + clientId +
                ", connId=" + connId +
                ", sessionId='" + sessionId + '\'' +
                ", content=" + (content != null ? content.length + " bytes" : "null") +
                '}';
    }
}