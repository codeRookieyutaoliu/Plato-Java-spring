package plato.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 确认消息
 * <p>
 * 用于确认消息接收的ACK消息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AckMessage extends AbstractMessage {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 确认状态码
     */
    private int code;
    
    /**
     * 确认消息
     */
    private String msg;
    
    /**
     * 原始消息类型
     */
    private MessageType originType;
    
    /**
     * 原始消息ID
     */
    private String originMessageId;
    
    /**
     * 连接ID
     */
    private String connectionId;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 默认构造函数
     */
    public AckMessage() {
        super();
    }
    
    /**
     * 成功确认构造函数
     *
     * @param originType 原始消息类型
     * @param originMessageId 原始消息ID
     */
    public AckMessage(MessageType originType, String originMessageId) {
        this(0, "成功", originType, originMessageId);
    }
    
    /**
     * 构造函数
     *
     * @param code 确认状态码
     * @param msg 确认消息
     * @param originType 原始消息类型
     * @param originMessageId 原始消息ID
     */
    public AckMessage(int code, String msg, MessageType originType, String originMessageId) {
        super();
        this.code = code;
        this.msg = msg;
        this.originType = originType;
        this.originMessageId = originMessageId;
    }
    
    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    @Override
    public MessageType getType() {
        return MessageType.ACK;
    }
    
    /**
     * 克隆消息
     *
     * @return 消息的深拷贝
     */
    @Override
    public Message clone() {
        AckMessage clone = new AckMessage();
        clone.setMessageId(this.getMessageId());
        clone.setTimestamp(this.getTimestamp());
        clone.setSenderId(this.getSenderId());
        clone.setReceiverId(this.getReceiverId());
        clone.setCode(this.getCode());
        clone.setMsg(this.getMsg());
        clone.setOriginType(this.getOriginType());
        clone.setOriginMessageId(this.getOriginMessageId());
        clone.setConnectionId(this.getConnectionId());
        clone.setClientId(this.getClientId());
        clone.setSessionId(this.getSessionId());
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
               originType != null && 
               originMessageId != null && !originMessageId.isEmpty();
    }
    
    /**
     * 获取消息优先级
     * ACK消息优先级较高
     *
     * @return 消息优先级
     */
    @Override
    public int getPriority() {
        return -5; // 较高的优先级，但低于心跳
    }
    
    /**
     * 判断确认是否成功
     *
     * @return 如果确认成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return code == 0;
    }
} 