package plato.message.domain.factory;

import com.google.protobuf.InvalidProtocolBufferException;
import plato.message.domain.entity.*;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.Message.MsgCmd;

/**
 * 消息工厂
 * <p>
 * 用于创建和解析各种类型的消息
 * </p>
 */
public class MessageFactory {
    
    /**
     * 私有构造函数，防止实例化
     */
    private MessageFactory() {
    }
    
    /**
     * 从Protocol Buffers命令消息创建领域消息对象
     *
     * @param bytes Protocol Buffers消息的字节数组
     * @return 领域消息对象
     * @throws InvalidProtocolBufferException 如果解析失败
     * @throws UnsupportedOperationException 如果消息类型不支持
     */
    public static Message createFromProtoBytes(byte[] bytes) throws InvalidProtocolBufferException {
        MsgCmd msgCmd = MsgCmd.parseFrom(bytes);
        int cmdType = msgCmd.getCmdType();
        
        MessageType type = MessageType.fromCode(cmdType);
        if (type == null) {
            throw new UnsupportedOperationException("不支持的消息类型: " + cmdType);
        }
        
        byte[] payload = msgCmd.getPayload().toByteArray();
        
        switch (type) {
            case LOGIN:
                return LoginMessage.fromProtoBytes(payload);
            case HEARTBEAT:
                return HeartbeatMessage.fromProtoBytes(payload);
            case RECONNECT:
                return ReconnectMessage.fromProtoBytes(payload);
            case ACK:
                return AckMessage.fromProtoBytes(payload);
            case UP:
                return UpMessage.fromProtoBytes(payload);
            case PUSH:
                return PushMessage.fromProtoBytes(payload);
            default:
                throw new UnsupportedOperationException("尚未实现的消息类型: " + type);
        }
    }
    
    /**
     * 创建Protocol Buffers命令消息
     *
     * @param message 领域消息对象
     * @return Protocol Buffers命令消息的字节数组
     */
    public static byte[] createProtoBytes(Message message) {
        MessageType type = message.getType();
        
        MsgCmd msgCmd = MsgCmd.newBuilder()
                .setCmdType(type.getCode())
                .setPayload(com.google.protobuf.ByteString.copyFrom(message.toBytes()))
                .build();
        
        return msgCmd.toByteArray();
    }
    
    /**
     * 创建登录消息
     *
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @param token 登录令牌
     * @return 登录消息对象
     */
    public static LoginMessage createLoginMessage(String uid, String deviceId, String token) {
        return new LoginMessage(uid, deviceId, token);
    }
    
    /**
     * 创建心跳消息
     *
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @return 心跳消息对象
     */
    public static HeartbeatMessage createHeartbeatMessage(String uid, String deviceId) {
        return new HeartbeatMessage(uid, deviceId);
    }
    
    /**
     * 创建重连消息
     *
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @param connId 连接ID
     * @return 重连消息对象
     */
    public static ReconnectMessage createReconnectMessage(String uid, String deviceId, long connId) {
        return new ReconnectMessage(uid, deviceId, connId);
    }
    
    /**
     * 创建确认消息
     *
     * @param code 状态码
     * @param msg 消息内容
     * @param ackType 确认的消息类型
     * @param connId 连接ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @param msgId 消息ID
     * @return 确认消息对象
     */
    public static AckMessage createAckMessage(int code, String msg, MessageType ackType, long connId, long clientId, long sessionId, long msgId) {
        return new AckMessage(code, msg, ackType, connId, clientId, sessionId, msgId);
    }
    
    /**
     * 创建上行消息
     *
     * @param clientId 客户端ID
     * @param connId 连接ID
     * @param sessionId 会话ID
     * @param content 消息内容
     * @return 上行消息对象
     */
    public static UpMessage createUpMessage(long clientId, long connId, String sessionId, byte[] content) {
        return new UpMessage(clientId, connId, sessionId, content);
    }
    
    /**
     * 创建推送消息
     *
     * @param msgId 消息ID
     * @param sessionId 会话ID
     * @param content 消息内容
     * @return 推送消息对象
     */
    public static PushMessage createPushMessage(long msgId, long sessionId, byte[] content) {
        return new PushMessage(msgId, sessionId, content);
    }
} 