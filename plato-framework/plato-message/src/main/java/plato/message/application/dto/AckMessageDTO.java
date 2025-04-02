package plato.message.application.dto;

import plato.message.domain.valueobject.MessageType;

/**
 * 确认消息DTO
 * <p>
 * 包含确认消息的所有数据
 * </p>
 */
public class AckMessageDTO extends MessageDTO {
    
    /**
     * 序列化版本UID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private final int code;
    
    /**
     * 消息内容
     */
    private final String msg;
    
    /**
     * 确认的消息类型
     */
    private final MessageType ackType;
    
    /**
     * 连接ID
     */
    private final long connId;
    
    /**
     * 客户端ID
     */
    private final long clientId;
    
    /**
     * 会话ID
     */
    private final long sessionId;
    
    /**
     * 消息ID
     */
    private final long msgId;
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 消息时间戳
     * @param type 消息类型
     * @param code 状态码
     * @param msg 消息内容
     * @param ackType 确认的消息类型
     * @param connId 连接ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @param msgId 消息ID
     */
    public AckMessageDTO(String id, long timestamp, MessageType type, int code, String msg, MessageType ackType, long connId, long clientId, long sessionId, long msgId) {
        super(id, timestamp, type);
        this.code = code;
        this.msg = msg;
        this.ackType = ackType;
        this.connId = connId;
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.msgId = msgId;
    }
    
    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取消息内容
     *
     * @return 消息内容
     */
    public String getMsg() {
        return msg;
    }
    
    /**
     * 获取确认的消息类型
     *
     * @return 确认的消息类型
     */
    public MessageType getAckType() {
        return ackType;
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
     * 获取客户端ID
     *
     * @return 客户端ID
     */
    public long getClientId() {
        return clientId;
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
     * 获取消息ID
     *
     * @return 消息ID
     */
    public long getMsgId() {
        return msgId;
    }
    
    @Override
    public String toString() {
        return "AckMessageDTO{" +
                "id='" + getId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", type=" + getType() +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", ackType=" + ackType +
                ", connId=" + connId +
                ", clientId=" + clientId +
                ", sessionId=" + sessionId +
                ", msgId=" + msgId +
                '}';
    }
}