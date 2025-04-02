package plato.message.domain.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.Message.ReconnectMsg;

/**
 * 重连消息
 * <p>
 * 客户端断线重连时发送的消息
 * </p>
 */
public class ReconnectMessage extends AbstractMessage {
    
    /**
     * 用户ID
     */
    private final String uid;
    
    /**
     * 设备ID
     */
    private final String deviceId;
    
    /**
     * 连接ID
     */
    private final long connId;
    
    /**
     * 构造函数
     *
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @param connId 连接ID
     */
    public ReconnectMessage(String uid, String deviceId, long connId) {
        super();
        this.uid = uid;
        this.deviceId = deviceId;
        this.connId = connId;
    }
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 时间戳
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @param connId 连接ID
     */
    public ReconnectMessage(String id, long timestamp, String uid, String deviceId, long connId) {
        super(id, timestamp);
        this.uid = uid;
        this.deviceId = deviceId;
        this.connId = connId;
    }
    
    /**
     * 从Protocol Buffers消息创建重连消息
     *
     * @param bytes Protocol Buffers消息的字节数组
     * @return 重连消息对象
     * @throws InvalidProtocolBufferException 如果解析失败
     */
    public static ReconnectMessage fromProtoBytes(byte[] bytes) throws InvalidProtocolBufferException {
        ReconnectMsg reconnectMsg = ReconnectMsg.parseFrom(bytes);
        return new ReconnectMessage(
                reconnectMsg.getId(),
                reconnectMsg.getTimestamp(),
                reconnectMsg.getUid(),
                reconnectMsg.getDeviceId(),
                reconnectMsg.getHead().getConnId()
        );
    }
    
    @Override
    public MessageType getType() {
        return MessageType.RECONNECT;
    }
    
    @Override
    public byte[] toBytes() {
        ReconnectMsg reconnectMsg = ReconnectMsg.newBuilder()
                .setId(getId())
                .setTimestamp(getTimestamp())
                .setUid(uid)
                .setDeviceId(deviceId)
                .setHead(ReconnectMsg.ReconnectMsgHead.newBuilder().setConnId(connId).build())
                .build();
        return reconnectMsg.toByteArray();
    }
    
    public String getUid() {
        return uid;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public long getConnId() {
        return connId;
    }
} 