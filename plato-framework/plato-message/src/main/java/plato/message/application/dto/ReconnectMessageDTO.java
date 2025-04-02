package plato.message.application.dto;

import plato.message.domain.valueobject.MessageType;

/**
 * 重连消息DTO
 * <p>
 * 包含重连消息的所有数据
 * </p>
 */
public class ReconnectMessageDTO extends MessageDTO {
    
    /**
     * 序列化版本UID
     */
    private static final long serialVersionUID = 1L;
    
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
     * @param id 消息ID
     * @param timestamp 消息时间戳
     * @param type 消息类型
     * @param uid 用户ID
     * @param deviceId 设备ID
     * @param connId 连接ID
     */
    public ReconnectMessageDTO(String id, long timestamp, MessageType type, String uid, String deviceId, long connId) {
        super(id, timestamp, type);
        this.uid = uid;
        this.deviceId = deviceId;
        this.connId = connId;
    }
    
    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUid() {
        return uid;
    }
    
    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }
    
    /**
     * 获取连接ID
     *
     * @return 连接ID
     */
    public long getConnId() {
        return connId;
    }
    
    @Override
    public String toString() {
        return "ReconnectMessageDTO{" +
                "id='" + getId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", type=" + getType() +
                ", uid='" + uid + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", connId=" + connId +
                '}';
    }
}