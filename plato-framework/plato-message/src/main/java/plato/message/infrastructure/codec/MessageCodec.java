package plato.message.infrastructure.codec;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import plato.message.domain.entity.*;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.AckMsg;
import plato.message.proto.CmdType;
import plato.message.proto.HeartbeatMsg;
import plato.message.proto.LoginMsg;
import plato.message.proto.MsgCmd;
import plato.message.proto.PushMsg;
import plato.message.proto.ReconnectMsg;
import plato.message.proto.UpMsg;
import plato.message.proto.UpMsgHead;

/**
 * 消息编解码器
 * <p>
 * 负责Protocol Buffers消息和领域模型消息之间的转换
 * </p>
 */
public class MessageCodec {
    
    /**
     * 私有构造函数，防止实例化
     */
    private MessageCodec() {
    }
    
    /**
     * 将领域模型消息编码为Protocol Buffers消息
     *
     * @param message 领域模型消息
     * @return Protocol Buffers消息的字节数组
     */
    public static byte[] encode(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("消息不能为空");
        }
        
        CmdType cmdType = convertToCmdType(message.getType());
        ByteString payload = ByteString.copyFrom(encodePayload(message));
        
        MsgCmd msgCmd = MsgCmd.newBuilder()
                .setCmdType(cmdType.getNumber())
                .setPayload(payload)
                .build();
        
        return msgCmd.toByteArray();
    }
    
    /**
     * 将Protocol Buffers消息解码为领域模型消息
     *
     * @param bytes Protocol Buffers消息的字节数组
     * @return 领域模型消息
     * @throws InvalidProtocolBufferException 如果解析失败
     */
    public static Message decode(byte[] bytes) throws InvalidProtocolBufferException {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("消息字节数组不能为空");
        }
        
        MsgCmd msgCmd = MsgCmd.parseFrom(bytes);
        int cmdTypeValue = msgCmd.getCmdType();
        CmdType cmdType = CmdType.forNumber(cmdTypeValue);
        
        if (cmdType == null) {
            throw new IllegalArgumentException("未知的消息类型: " + cmdTypeValue);
        }
        
        byte[] payload = msgCmd.getPayload().toByteArray();
        return decodePayload(cmdType, payload);
    }
    
    /**
     * 将消息类型转换为Protocol Buffers的CmdType
     *
     * @param type 消息类型
     * @return Protocol Buffers的CmdType
     */
    private static CmdType convertToCmdType(MessageType type) {
        if (type == null) {
            throw new IllegalArgumentException("消息类型不能为空");
        }
        
        return switch (type) {
            case LOGIN -> CmdType.LOGIN;
            case HEARTBEAT -> CmdType.HEARTBEAT;
            case RECONNECT -> CmdType.RECONNECT;
            case ACK -> CmdType.ACK;
            case UP -> CmdType.UP;
            case PUSH -> CmdType.PUSH;
        };
    }
    
    /**
     * 将Protocol Buffers的CmdType转换为消息类型
     *
     * @param cmdType Protocol Buffers的CmdType
     * @return 消息类型
     */
    private static MessageType convertToMessageType(CmdType cmdType) {
        if (cmdType == null) {
            throw new IllegalArgumentException("Protocol Buffers的CmdType不能为空");
        }
        
        return switch (cmdType) {
            case LOGIN -> MessageType.LOGIN;
            case HEARTBEAT -> MessageType.HEARTBEAT;
            case RECONNECT -> MessageType.RECONNECT;
            case ACK -> MessageType.ACK;
            case UP -> MessageType.UP;
            case PUSH -> MessageType.PUSH;
            default -> throw new IllegalArgumentException("不支持的Protocol Buffers CmdType: " + cmdType);
        };
    }
    
    /**
     * 将领域模型消息编码为特定类型的Protocol Buffers消息
     *
     * @param message 领域模型消息
     * @return Protocol Buffers消息的字节数组
     */
    private static byte[] encodePayload(Message message) {
        return switch (message.getType()) {
            case LOGIN -> encodeLoginMessage((LoginMessage) message);
            case HEARTBEAT -> encodeHeartbeatMessage((HeartbeatMessage) message);
            case RECONNECT -> encodeReconnectMessage((ReconnectMessage) message);
            case ACK -> encodeAckMessage((AckMessage) message);
            case UP -> encodeUpMessage((UpMessage) message);
            case PUSH -> encodePushMessage((PushMessage) message);
        };
    }
    
    /**
     * 将Protocol Buffers消息解码为特定类型的领域模型消息
     *
     * @param cmdType Protocol Buffers的CmdType
     * @param payload Protocol Buffers消息的负载
     * @return 领域模型消息
     * @throws InvalidProtocolBufferException 如果解析失败
     */
    private static Message decodePayload(CmdType cmdType, byte[] payload) throws InvalidProtocolBufferException {
        return switch (cmdType) {
            case LOGIN -> decodeLoginMessage(payload);
            case HEARTBEAT -> decodeHeartbeatMessage(payload);
            case RECONNECT -> decodeReconnectMessage(payload);
            case ACK -> decodeAckMessage(payload);
            case UP -> decodeUpMessage(payload);
            case PUSH -> decodePushMessage(payload);
            default -> throw new IllegalArgumentException("不支持的Protocol Buffers CmdType: " + cmdType);
        };
    }
    
    // 以下是各种具体消息类型的编解码方法
    
    private static byte[] encodeLoginMessage(LoginMessage message) {
        LoginMsg loginMsg = LoginMsg.newBuilder()
                .setId(message.getId())
                .setTimestamp(message.getTimestamp())
                .setUid(message.getUid())
                .setDeviceId(message.getDeviceId())
                .setToken(message.getToken())
                .build();
        return loginMsg.toByteArray();
    }
    
    private static LoginMessage decodeLoginMessage(byte[] payload) throws InvalidProtocolBufferException {
        LoginMsg loginMsg = LoginMsg.parseFrom(payload);
        return new LoginMessage(
                loginMsg.getId(),
                loginMsg.getTimestamp(),
                loginMsg.getUid(),
                loginMsg.getDeviceId(),
                loginMsg.getToken()
        );
    }
    
    private static byte[] encodeHeartbeatMessage(HeartbeatMessage message) {
        HeartbeatMsg heartbeatMsg = HeartbeatMsg.newBuilder()
                .setId(message.getId())
                .setTimestamp(message.getTimestamp())
                .setUid(message.getUid())
                .setDeviceId(message.getDeviceId())
                .build();
        return heartbeatMsg.toByteArray();
    }
    
    private static HeartbeatMessage decodeHeartbeatMessage(byte[] payload) throws InvalidProtocolBufferException {
        HeartbeatMsg heartbeatMsg = HeartbeatMsg.parseFrom(payload);
        return new HeartbeatMessage(
                heartbeatMsg.getId(),
                heartbeatMsg.getTimestamp(),
                heartbeatMsg.getUid(),
                heartbeatMsg.getDeviceId()
        );
    }
    
    private static byte[] encodeReconnectMessage(ReconnectMessage message) {
        ReconnectMsg reconnectMsg = ReconnectMsg.newBuilder()
                .setId(message.getId())
                .setTimestamp(message.getTimestamp())
                .setUid(message.getUid())
                .setDeviceId(message.getDeviceId())
                .setConnId(message.getConnId())
                .build();
        return reconnectMsg.toByteArray();
    }
    
    private static ReconnectMessage decodeReconnectMessage(byte[] payload) throws InvalidProtocolBufferException {
        ReconnectMsg reconnectMsg = ReconnectMsg.parseFrom(payload);
        return new ReconnectMessage(
                reconnectMsg.getId(),
                reconnectMsg.getTimestamp(),
                reconnectMsg.getUid(),
                reconnectMsg.getDeviceId(),
                reconnectMsg.getConnId()
        );
    }
    
    private static byte[] encodeAckMessage(AckMessage message) {
        AckMsg ackMsg = AckMsg.newBuilder()
                .setId(message.getId())
                .setTimestamp(message.getTimestamp())
                .setCode(message.getCode())
                .setMsg(message.getMsg())
                .setType(convertToCmdType(message.getAckType()).getNumber())
                .setConnId(message.getConnId())
                .setClientId(message.getClientId())
                .setSessionId(message.getSessionId())
                .setMsgId(message.getMsgId())
                .build();
        return ackMsg.toByteArray();
    }
    
    private static AckMessage decodeAckMessage(byte[] payload) throws InvalidProtocolBufferException {
        AckMsg ackMsg = AckMsg.parseFrom(payload);
        return new AckMessage(
                ackMsg.getId(),
                ackMsg.getTimestamp(),
                ackMsg.getCode(),
                ackMsg.getMsg(),
                convertToMessageType(CmdType.forNumber(ackMsg.getType())),
                ackMsg.getConnId(),
                ackMsg.getClientId(),
                ackMsg.getSessionId(),
                ackMsg.getMsgId()
        );
    }
    
    private static byte[] encodeUpMessage(UpMessage message) {
        UpMsgHead head = UpMsgHead.newBuilder()
                .setClientId(message.getClientId())
                .setConnId(message.getConnId())
                .setSessionId(message.getSessionId())
                .build();
        
        UpMsg upMsg = UpMsg.newBuilder()
                .setId(message.getId())
                .setTimestamp(message.getTimestamp())
                .setHead(head)
                .setBody(ByteString.copyFrom(message.getContent()))
                .build();
        
        return upMsg.toByteArray();
    }
    
    private static UpMessage decodeUpMessage(byte[] payload) throws InvalidProtocolBufferException {
        UpMsg upMsg = UpMsg.parseFrom(payload);
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
    
    private static byte[] encodePushMessage(PushMessage message) {
        PushMsg pushMsg = PushMsg.newBuilder()
                .setId(message.getId())
                .setTimestamp(message.getTimestamp())
                .setMsgId(message.getMsgId())
                .setSessionId(message.getSessionId())
                .setContent(ByteString.copyFrom(message.getContent()))
                .build();
        
        return pushMsg.toByteArray();
    }
    
    private static PushMessage decodePushMessage(byte[] payload) throws InvalidProtocolBufferException {
        PushMsg pushMsg = PushMsg.parseFrom(payload);
        
        return new PushMessage(
                pushMsg.getId(),
                pushMsg.getTimestamp(),
                pushMsg.getMsgId(),
                pushMsg.getSessionId(),
                pushMsg.getContent().toByteArray()
        );
    }
}