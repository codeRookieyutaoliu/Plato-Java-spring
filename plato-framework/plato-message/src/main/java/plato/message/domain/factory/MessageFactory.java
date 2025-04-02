package plato.message.domain.factory;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.domain.entity.*;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.Message.MsgCmd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息工厂
 * <p>
 * 用于创建和解析各种类型的消息
 * </p>
 * 
 * 对应Go项目中的messageFactory
 */
public class MessageFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageFactory.class);
    
    /**
     * 消息ID生成器，用于生成唯一的消息ID
     */
    private static final AtomicLong MESSAGE_ID_GENERATOR = new AtomicLong(1);
    
    /**
     * 消息验证失败的错误码
     */
    private static final int VALIDATION_ERROR_CODE = 400;
    
    /**
     * 消息类型缓存，用于缓存消息类型对应的Class对象
     */
    private static final Map<MessageType, Class<? extends Message>> MESSAGE_TYPE_CACHE = new ConcurrentHashMap<>();
    
    static {
        // 初始化消息类型缓存
        MESSAGE_TYPE_CACHE.put(MessageType.LOGIN, LoginMessage.class);
        MESSAGE_TYPE_CACHE.put(MessageType.HEARTBEAT, HeartbeatMessage.class);
        MESSAGE_TYPE_CACHE.put(MessageType.RECONNECT, ReconnectMessage.class);
        MESSAGE_TYPE_CACHE.put(MessageType.ACK, AckMessage.class);
        MESSAGE_TYPE_CACHE.put(MessageType.UP, UpMessage.class);
        MESSAGE_TYPE_CACHE.put(MessageType.PUSH, PushMessage.class);
    }
    
    /**
     * 私有构造函数，防止实例化
     */
    private MessageFactory() {
    }
    
    /**
     * 生成唯一的消息ID
     * 
     * @return 唯一的消息ID
     */
    public static String generateMessageId() {
        return String.valueOf(MESSAGE_ID_GENERATOR.getAndIncrement());
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
        if (bytes == null || bytes.length == 0) {
            LOGGER.error("消息字节数组为空");
            throw new IllegalArgumentException("消息字节数组不能为空");
        }
        
        try {
            MsgCmd msgCmd = MsgCmd.parseFrom(bytes);
            int cmdType = msgCmd.getCmdType();
            
            MessageType type = MessageType.fromCode(cmdType);
            if (type == null) {
                LOGGER.error("不支持的消息类型: {}", cmdType);
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
                    LOGGER.error("尚未实现的消息类型: {}", type);
                    throw new UnsupportedOperationException("尚未实现的消息类型: " + type);
            }
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("解析Protocol Buffers消息失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("创建消息失败", e);
            throw new RuntimeException("创建消息失败", e);
        }
    }
    
    /**
     * 创建Protocol Buffers命令消息
     *
     * @param message 领域消息对象
     * @return Protocol Buffers命令消息的字节数组
     */
    public static byte[] createProtoBytes(Message message) {
        if (message == null) {
            LOGGER.error("消息对象为空");
            throw new IllegalArgumentException("消息对象不能为空");
        }
        
        try {
            MessageType type = message.getType();
            
            MsgCmd msgCmd = MsgCmd.newBuilder()
                    .setCmdType(type.getCode())
                    .setPayload(com.google.protobuf.ByteString.copyFrom(message.toBytes()))
                    .build();
            
            return msgCmd.toByteArray();
        } catch (Exception e) {
            LOGGER.error("创建Protocol Buffers消息失败: {}", message.getType(), e);
            throw new RuntimeException("创建Protocol Buffers消息失败", e);
        }
    }
    
    /**
     * 根据消息类型获取消息类
     * 
     * @param type 消息类型
     * @return 消息类
     */
    public static Class<? extends Message> getMessageClass(MessageType type) {
        return MESSAGE_TYPE_CACHE.get(type);
    }
    
    /**
     * 验证消息的基本字段
     * 
     * @param message 要验证的消息
     * @return 如果验证通过返回true，否则返回false
     */
    public static boolean validateMessage(Message message) {
        if (message == null) {
            return false;
        }
        
        // 检查消息ID
        if (message.getId() == null || message.getId().isEmpty()) {
            LOGGER.warn("消息ID为空: {}", message.getType());
            return false;
        }
        
        // 检查时间戳
        if (message.getTimestamp() <= 0) {
            LOGGER.warn("消息时间戳无效: {}, timestamp={}", message.getType(), message.getTimestamp());
            return false;
        }
        
        return true;
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
        LoginMessage message = new LoginMessage(uid, deviceId, token);
        
        // 如果未设置消息ID，生成一个
        if (message.getId() == null || message.getId().isEmpty()) {
            ((AbstractMessage)message).setId(generateMessageId());
        }
        
        return message;
    }
    
    /**
     * 创建心跳消息
     *
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @return 心跳消息对象
     */
    public static HeartbeatMessage createHeartbeatMessage(String uid, String deviceId) {
        HeartbeatMessage message = new HeartbeatMessage(uid, deviceId);
        
        // 如果未设置消息ID，生成一个
        if (message.getId() == null || message.getId().isEmpty()) {
            ((AbstractMessage)message).setId(generateMessageId());
        }
        
        return message;
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
        ReconnectMessage message = new ReconnectMessage(uid, deviceId, connId);
        
        // 如果未设置消息ID，生成一个
        if (message.getId() == null || message.getId().isEmpty()) {
            ((AbstractMessage)message).setId(generateMessageId());
        }
        
        return message;
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
        AckMessage message = new AckMessage(code, msg, ackType, connId, clientId, sessionId, msgId);
        
        // 如果未设置消息ID，生成一个
        if (message.getId() == null || message.getId().isEmpty()) {
            ((AbstractMessage)message).setId(generateMessageId());
        }
        
        return message;
    }
    
    /**
     * 创建成功的确认消息
     * 
     * @param ackType 确认的消息类型
     * @param connId 连接ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @param msgId 消息ID
     * @return 确认消息对象
     */
    public static AckMessage createSuccessAck(MessageType ackType, long connId, long clientId, long sessionId, long msgId) {
        return createAckMessage(0, "成功", ackType, connId, clientId, sessionId, msgId);
    }
    
    /**
     * 创建失败的确认消息
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
    public static AckMessage createFailureAck(int code, String msg, MessageType ackType, long connId, long clientId, long sessionId, long msgId) {
        return createAckMessage(code, msg, ackType, connId, clientId, sessionId, msgId);
    }
    
    /**
     * 创建验证失败的确认消息
     * 
     * @param errorMsg 错误信息
     * @param ackType 确认的消息类型
     * @param connId 连接ID
     * @return 确认消息对象
     */
    public static AckMessage createValidationFailureAck(String errorMsg, MessageType ackType, long connId) {
        return createAckMessage(VALIDATION_ERROR_CODE, errorMsg, ackType, connId, 0, 0, 0);
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
        UpMessage message = new UpMessage(clientId, connId, sessionId, content);
        
        // 如果未设置消息ID，生成一个
        if (message.getId() == null || message.getId().isEmpty()) {
            ((AbstractMessage)message).setId(generateMessageId());
        }
        
        return message;
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
        PushMessage message = new PushMessage(msgId, sessionId, content);
        
        // 如果未设置消息ID，生成一个
        if (message.getId() == null || message.getId().isEmpty()) {
            ((AbstractMessage)message).setId(generateMessageId());
        }
        
        return message;
    }
} 