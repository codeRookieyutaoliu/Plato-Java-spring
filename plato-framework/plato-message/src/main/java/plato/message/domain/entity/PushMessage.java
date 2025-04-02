package plato.message.domain.entity;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.Message.PushMsg;

/**
 * 下行推送消息
 * <p>
 * 服务器推送给客户端的业务消息
 * </p>
 */
public class PushMessage extends AbstractMessage {
    
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
     * @param msgId 消息ID
     * @param sessionId 会话ID
     * @param content 消息内容
     */
    public PushMessage(long msgId, long sessionId, byte[] content) {
        super();
        this.msgId = msgId;
        this.sessionId = sessionId;
        this.content = content;
    }
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 时间戳
     * @param msgId 消息ID
     * @param sessionId 会话ID
     * @param content 消息内容
     */
    public PushMessage(String id, long timestamp, long msgId, long sessionId, byte[] content) {
        super(id, timestamp);
        this.msgId = msgId;
        this.sessionId = sessionId;
        this.content = content;
    }
    
    /**
     * 从Protocol Buffers消息创建推送消息
     *
     * @param bytes Protocol Buffers消息的字节数组
     * @return 推送消息对象
     * @throws InvalidProtocolBufferException 如果解析失败
     */
    public static PushMessage fromProtoBytes(byte[] bytes) throws InvalidProtocolBufferException {
        PushMsg pushMsg = PushMsg.parseFrom(bytes);
        
        return new PushMessage(
                pushMsg.getId(),
                pushMsg.getTimestamp(),
                pushMsg.getMsgId(),
                pushMsg.getSessionId(),
                pushMsg.getContent().toByteArray()
        );
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PUSH;
    }
    
    @Override
    public byte[] toBytes() {
        PushMsg pushMsg = PushMsg.newBuilder()
                .setId(getId())
                .setTimestamp(getTimestamp())
                .setMsgId(msgId)
                .setSessionId(sessionId)
                .setContent(ByteString.copyFrom(content))
                .build();
                
        return pushMsg.toByteArray();
    }
    
    public long getMsgId() {
        return msgId;
    }
    
    public long getSessionId() {
        return sessionId;
    }
    
    public byte[] getContent() {
        return content;
    }
} 