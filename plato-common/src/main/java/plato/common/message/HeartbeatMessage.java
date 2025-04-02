package plato.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 心跳消息
 * <p>
 * 用于保持连接活跃的心跳消息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HeartbeatMessage extends AbstractMessage {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 连接ID
     */
    private String connectionId;
    
    /**
     * 默认构造函数
     */
    public HeartbeatMessage() {
        super();
    }
    
    /**
     * 构造函数
     *
     * @param clientId 客户端ID
     * @param connectionId 连接ID
     */
    public HeartbeatMessage(String clientId, String connectionId) {
        super();
        this.clientId = clientId;
        this.connectionId = connectionId;
    }
    
    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    @Override
    public MessageType getType() {
        return MessageType.HEARTBEAT;
    }
    
    /**
     * 克隆消息
     *
     * @return 消息的深拷贝
     */
    @Override
    public Message clone() {
        HeartbeatMessage clone = new HeartbeatMessage();
        clone.setMessageId(this.getMessageId());
        clone.setTimestamp(this.getTimestamp());
        clone.setSenderId(this.getSenderId());
        clone.setReceiverId(this.getReceiverId());
        clone.setClientId(this.getClientId());
        clone.setConnectionId(this.getConnectionId());
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
               clientId != null && !clientId.isEmpty() && 
               connectionId != null && !connectionId.isEmpty();
    }
    
    /**
     * 获取消息优先级
     * 心跳消息优先级较高
     *
     * @return 消息优先级
     */
    @Override
    public int getPriority() {
        return -10; // 较高的优先级
    }
} 