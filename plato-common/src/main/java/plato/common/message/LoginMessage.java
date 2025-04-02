package plato.common.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录消息
 * <p>
 * 用户登录时发送的消息
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginMessage extends AbstractMessage {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 客户端版本
     */
    private String clientVersion;
    
    /**
     * 默认构造函数
     */
    public LoginMessage() {
        super();
    }
    
    /**
     * 构造函数
     *
     * @param username 用户名
     * @param password 密码
     * @param deviceId 设备ID
     */
    public LoginMessage(String username, String password, String deviceId) {
        super();
        this.username = username;
        this.password = password;
        this.deviceId = deviceId;
    }
    
    /**
     * 构造函数
     *
     * @param username 用户名
     * @param password 密码
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param clientVersion 客户端版本
     */
    public LoginMessage(String username, String password, String deviceId, 
                         String deviceType, String clientVersion) {
        this(username, password, deviceId);
        this.deviceType = deviceType;
        this.clientVersion = clientVersion;
    }
    
    /**
     * 获取消息类型
     *
     * @return 消息类型
     */
    @Override
    public MessageType getType() {
        return MessageType.LOGIN;
    }
    
    /**
     * 克隆消息
     *
     * @return 消息的深拷贝
     */
    @Override
    public Message clone() {
        LoginMessage clone = new LoginMessage();
        clone.setMessageId(this.getMessageId());
        clone.setTimestamp(this.getTimestamp());
        clone.setSenderId(this.getSenderId());
        clone.setReceiverId(this.getReceiverId());
        clone.setUsername(this.getUsername());
        clone.setPassword(this.getPassword());
        clone.setDeviceId(this.getDeviceId());
        clone.setDeviceType(this.getDeviceType());
        clone.setClientVersion(this.getClientVersion());
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
               username != null && !username.isEmpty() && 
               deviceId != null && !deviceId.isEmpty();
    }
    
    /**
     * 重写toString方法，保护敏感信息
     *
     * @return 消息的字符串表示
     */
    @Override
    public String toString() {
        return "LoginMessage{" +
                "type=" + getType() +
                ", messageId='" + getMessageId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", username='" + username + '\'' +
                ", password='********'" +
                ", deviceId='" + deviceId + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                '}';
    }
} 