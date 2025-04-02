package plato.message.domain.entity;

import com.google.protobuf.InvalidProtocolBufferException;
import plato.message.domain.valueobject.MessageType;
import plato.message.proto.Message.LoginMsg;

/**
 * 登录消息
 * <p>
 * 客户端登录服务器时发送的消息
 * </p>
 */
public class LoginMessage extends AbstractMessage {
    
    /**
     * 用户ID
     */
    private final String uid;
    
    /**
     * 设备ID
     */
    private final String deviceId;
    
    /**
     * 登录令牌
     */
    private final String token;
    
    /**
     * 构造函数
     *
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @param token 登录令牌
     */
    public LoginMessage(String uid, String deviceId, String token) {
        super();
        this.uid = uid;
        this.deviceId = deviceId;
        this.token = token;
    }
    
    /**
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 时间戳
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @param token 登录令牌
     */
    public LoginMessage(String id, long timestamp, String uid, String deviceId, String token) {
        super(id, timestamp);
        this.uid = uid;
        this.deviceId = deviceId;
        this.token = token;
    }
    
    /**
     * 从Protocol Buffers消息创建登录消息
     *
     * @param bytes Protocol Buffers消息的字节数组
     * @return 登录消息对象
     * @throws InvalidProtocolBufferException 如果解析失败
     */
    public static LoginMessage fromProtoBytes(byte[] bytes) throws InvalidProtocolBufferException {
        LoginMsg loginMsg = LoginMsg.parseFrom(bytes);
        return new LoginMessage(
                loginMsg.getId(),
                loginMsg.getTimestamp(),
                loginMsg.getUid(),
                loginMsg.getDeviceId(),
                loginMsg.getToken()
        );
    }
    
    @Override
    public MessageType getType() {
        return MessageType.LOGIN;
    }
    
    @Override
    public byte[] toBytes() {
        LoginMsg loginMsg = LoginMsg.newBuilder()
                .setId(getId())
                .setTimestamp(getTimestamp())
                .setUid(uid)
                .setDeviceId(deviceId)
                .setToken(token)
                .build();
        return loginMsg.toByteArray();
    }
    
    public String getUid() {
        return uid;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public String getToken() {
        return token;
    }
} 