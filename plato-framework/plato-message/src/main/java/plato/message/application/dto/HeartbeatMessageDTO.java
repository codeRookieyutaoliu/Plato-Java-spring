package plato.message.application.dto;

import plato.message.domain.valueobject.MessageType;

/**
 * 心跳消息DTO
 * <p>
 * 包含心跳消息的所有数据
 * </p>
 */
public class HeartbeatMessageDTO extends MessageDTO {
    
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
     * 构造函数
     *
     * @param id 消息ID
     * @param timestamp 消息时间戳
     * @param type 消息类型
     * @param uid 用户ID
     * @param deviceId 设备ID
     */
    public HeartbeatMessageDTO(String id, long timestamp, MessageType type, String uid, String deviceId) {
        super(id, timestamp, type);
        this.uid = uid;
        this.deviceId = deviceId;
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
    
    @Override
    public String toString() {
        return "HeartbeatMessageDTO{" +
                "id='" + getId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", type=" + getType() +
                ", uid='" + uid + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}