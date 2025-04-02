package plato.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 重连消息
 * <p>
 * 连接断开后重新连接时发送的消息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReconnectMessage extends AbstractMessage {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 连接ID
     */
    private String connectionId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 断开原因
     */
    private String disconnectReason;
    
    /**
     * 上次断开时间戳
     */
    private long disconnectTimestamp;
    
    /**
     * 默认构造函数
     */
    public ReconnectMessage() {
        super();
    }
    
    /**
     * 构造函数
     *
     * @param connectionId 连接ID
     * @param clientId 客户端ID
     */
    public ReconnectMessage(String connectionId, String clientId) {
        super();
        this.connectionId = connectionId;
        this.clientId = clientId;
        this.disconnectTimestamp = System.currentTimeMillis();
    }
    
    /**
     * 构造函数
     *
     * @param connectionId 连接ID
     * @param sessionId 会话ID
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @param deviceId 设备ID
     */
    public ReconnectMessage(String connectionId, String sessionId, String clientId, 
                            String userId, String deviceId) {
        super();
        this.connectionId = connectionId;
        this.sessionId = sessionId;
        this.clientId = clientId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.disconnectTimestamp = System.currentTimeMillis();
    }
    
    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    @Override
    public MessageType getType() {
        return MessageType.RECONNECT;
    }
    
    /**
     * 克隆消息
     *
     * @return 消息的深拷贝
     */
    @Override
    public Message clone() {
        ReconnectMessage clone = new ReconnectMessage();
        clone.setMessageId(this.getMessageId());
        clone.setTimestamp(this.getTimestamp());
        clone.setSenderId(this.getSenderId());
        clone.setReceiverId(this.getReceiverId());
        clone.setConnectionId(this.getConnectionId());
        clone.setSessionId(this.getSessionId());
        clone.setClientId(this.getClientId());
        clone.setUserId(this.getUserId());
        clone.setDeviceId(this.getDeviceId());
        clone.setDisconnectReason(this.getDisconnectReason());
        clone.setDisconnectTimestamp(this.getDisconnectTimestamp());
        return clone;
    }
    
    /**
     * 验证消息的有效性
     *
     * @return 如果消息有效返回true，否则返回false
     */
    @Override
    public boolean isValid() {
        return super.isValid() && 
               connectionId != null && !connectionId.isEmpty() && 
               clientId != null && !clientId.isEmpty();
    }
    
    /**
     * 获取消息优先级
     * 重连消息优先级较高
     *
     * @return 消息优先级
     */
    @Override
    public int getPriority() {
        return -5; // 较高的优先级，与ACK相同
    }
} 