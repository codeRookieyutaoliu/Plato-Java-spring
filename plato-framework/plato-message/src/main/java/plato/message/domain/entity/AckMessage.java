package plato.message.domain.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.Message.AckMsg;
import plato.message.proto.Message.CmdType;

/**
 * 确认消息
 * <p>
 * 用于确认消息的接收和处理状态
 * </p>
 */
public class AckMessage extends AbstractMessage {
    
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
     * @param code 状态码
     * @param msg 消息内容
     * @param ackType 确认的消息类型
     * @param connId 连接ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @param msgId 消息ID
     */
    public AckMessage(int code, String msg, MessageType ackType, long connId, long clientId, long sessionId, long msgId) {
        super();
        this.code = code;
        this.msg = msg;
        this.ackType = ackType;
        this.connId = connId;
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.msgId = msgId;
    }
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 时间戳
     * @param code 状态码
     * @param msg 消息内容
     * @param ackType 确认的消息类型
     * @param connId 连接ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @param msgId 消息ID
     */
    public AckMessage(String id, long timestamp, int code, String msg, MessageType ackType, long connId, long clientId, long sessionId, long msgId) {
        super(id, timestamp);
        this.code = code;
        this.msg = msg;
        this.ackType = ackType;
        this.connId = connId;
        this.clientId = clientId;
        this.sessionId = sessionId;
        this.msgId = msgId;
    }
    
    /**
     * 从Protocol Buffers消息创建确认消息
     *
     * @param bytes Protocol Buffers消息的字节数组
     * @return 确认消息对象
     * @throws InvalidProtocolBufferException 如果解析失败
     */
    public static AckMessage fromProtoBytes(byte[] bytes) throws InvalidProtocolBufferException {
        AckMsg ackMsg = AckMsg.parseFrom(bytes);
        CmdType cmdType = ackMsg.getType();
        MessageType ackType = MessageType.fromCode(cmdType.getNumber());
        
        return new AckMessage(
                ackMsg.getId(),
                ackMsg.getTimestamp(),
                ackMsg.getCode(),
                ackMsg.getMsg(),
                ackType,
                ackMsg.getConnId(),
                ackMsg.getClientId(),
                ackMsg.getSessionId(),
                ackMsg.getMsgId()
        );
    }
    
    @Override
    public MessageType getType() {
        return MessageType.ACK;
    }
    
    @Override
    public byte[] toBytes() {
        CmdType cmdType = CmdType.forNumber(ackType.getCode());
        if (cmdType == null) {
            cmdType = CmdType.UNRECOGNIZED;
        }
        
        AckMsg ackMsg = AckMsg.newBuilder()
                .setId(getId())
                .setTimestamp(getTimestamp())
                .setCode(code)
                .setMsg(msg)
                .setType(cmdType)
                .setConnId(connId)
                .setClientId(clientId)
                .setSessionId(sessionId)
                .setMsgId(msgId)
                .build();
        return ackMsg.toByteArray();
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public MessageType getAckType() {
        return ackType;
    }
    
    public long getConnId() {
        return connId;
    }
    
    public long getClientId() {
        return clientId;
    }
    
    public long getSessionId() {
        return sessionId;
    }
    
    public long getMsgId() {
        return msgId;
    }
}