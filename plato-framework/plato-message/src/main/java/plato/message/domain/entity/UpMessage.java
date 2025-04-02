package plato.message.domain.entity;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.Message.UpMsg;
import plato.message.proto.Message.UpMsgHead;

/**
 * 上行消息
 * <p>
 * 客户端发送给服务器的业务消息
 * </p>
 */
public class UpMessage extends AbstractMessage {
    
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
     * @param clientId 客户端ID
     * @param connId 连接ID
     * @param sessionId 会话ID
     * @param content 消息内容
     */
    public UpMessage(long clientId, long connId, String sessionId, byte[] content) {
        super();
        this.clientId = clientId;
        this.connId = connId;
        this.sessionId = sessionId;
        this.content = content;
    }
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 时间戳
     * @param clientId 客户端ID
     * @param connId 连接ID
     * @param sessionId 会话ID
     * @param content 消息内容
     */
    public UpMessage(String id, long timestamp, long clientId, long connId, String sessionId, byte[] content) {
        super(id, timestamp);
        this.clientId = clientId;
        this.connId = connId;
        this.sessionId = sessionId;
        this.content = content;
    }
    
    /**
     * 从Protocol Buffers消息创建上行消息
     *
     * @param bytes Protocol Buffers消息的字节数组
     * @return 上行消息对象
     * @throws InvalidProtocolBufferException 如果解析失败
     */
    public static UpMessage fromProtoBytes(byte[] bytes) throws InvalidProtocolBufferException {
        UpMsg upMsg = UpMsg.parseFrom(bytes);
        UpMsgHead head = upMsg.getHead();
        
        return new UpMessage(
                upMsg.getId(),
                upMsg.getTimestamp(),
                head.getClientId(),
                head.getConnId(),
                head.getSessionId(),
                upMsg.getBody().toByteArray()
        );
    }
    
    @Override
    public MessageType getType() {
        return MessageType.UP;
    }
    
    @Override
    public byte[] toBytes() {
        UpMsgHead head = UpMsgHead.newBuilder()
                .setClientId(clientId)
                .setConnId(connId)
                .setSessionId(sessionId)
                .build();
        
        UpMsg upMsg = UpMsg.newBuilder()
                .setId(getId())
                .setTimestamp(getTimestamp())
                .setHead(head)
                .setBody(ByteString.copyFrom(content))
                .build();
                
        return upMsg.toByteArray();
    }
    
    public long getClientId() {
        return clientId;
    }
    
    public long getConnId() {
        return connId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public byte[] getContent() {
        return content;
    }
} 