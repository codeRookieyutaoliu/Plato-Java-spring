package plato.message.domain.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.Message.HeartbeatMsg;

/**
 * 心跳消息
 * <p>
 * 用于客户端与服务器之间保持连接的心跳消息
 * </p>
 */
public class HeartbeatMessage extends AbstractMessage {
    
    /**
     * 用户ID
     */
    private final String uid;
    
    /**
     * 设备ID
     */
    private final String deviceId;
    
    /**
     * 构造函数
     *
     * @param uid 用户ID
     * @param deviceId 设备ID
     */
    public HeartbeatMessage(String uid, String deviceId) {
        super();
        this.uid = uid;
        this.deviceId = deviceId;
    }
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 时间戳
     * @param uid 用户ID
     * @param deviceId 设备ID
     */
    public HeartbeatMessage(String id, long timestamp, String uid, String deviceId) {
        super(id, timestamp);
        this.uid = uid;
        this.deviceId = deviceId;
    }
    
    /**
     * 从Protocol Buffers消息创建心跳消息
     *
     * @param bytes Protocol Buffers消息的字节数组
     * @return 心跳消息对象
     * @throws InvalidProtocolBufferException 如果解析失败
     */
    public static HeartbeatMessage fromProtoBytes(byte[] bytes) throws InvalidProtocolBufferException {
        HeartbeatMsg heartbeatMsg = HeartbeatMsg.parseFrom(bytes);
        return new HeartbeatMessage(
                heartbeatMsg.getId(),
                heartbeatMsg.getTimestamp(),
                heartbeatMsg.getUid(),
                heartbeatMsg.getDeviceId()
        );
    }
    
    @Override
    public MessageType getType() {
        return MessageType.HEARTBEAT;
    }
    
    @Override
    public byte[] toBytes() {
        HeartbeatMsg heartbeatMsg = HeartbeatMsg.newBuilder()
                .setId(getId())
                .setTimestamp(getTimestamp())
                .setUid(uid)
                .setDeviceId(deviceId)
                .build();
        return heartbeatMsg.toByteArray();
    }
    
    public String getUid() {
        return uid;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
} 