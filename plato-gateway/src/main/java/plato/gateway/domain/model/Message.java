package plato.gateway.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * 消息领域模型
 * 表示一个TCP消息，包含消息的基本信息和内容
 */
@Slf4j
@Getter
@Builder
public class Message {
    /**
     * 消息ID
     */
    private final long id;

    /**
     * 连接ID
     */
    private final long connectionId;

    /**
     * 消息类型
     */
    private final MessageType type;

    /**
     * 消息内容
     */
    private final byte[] payload;

    /**
     * 创建时间
     */
    private final Instant createTime;

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /**
         * 心跳消息
         */
        HEARTBEAT,

        /**
         * 业务消息
         */
        BUSINESS,

        /**
         * 系统消息
         */
        SYSTEM
    }

    /**
     * 构造函数
     *
     * @param id          消息ID
     * @param connectionId 连接ID
     * @param type        消息类型
     * @param payload     消息内容
     */
    public Message(long id, long connectionId, MessageType type, byte[] payload) {
        this.id = id;
        this.connectionId = connectionId;
        this.type = type;
        this.payload = payload;
        this.createTime = Instant.now();
    }

    /**
     * 获取消息大小
     *
     * @return 消息大小（字节）
     */
    public int getSize() {
        return payload != null ? payload.length : 0;
    }

    /**
     * 检查是否是心跳消息
     *
     * @return 是否是心跳消息
     */
    public boolean isHeartbeat() {
        return type == MessageType.HEARTBEAT;
    }

    /**
     * 检查是否是业务消息
     *
     * @return 是否是业务消息
     */
    public boolean isBusiness() {
        return type == MessageType.BUSINESS;
    }

    /**
     * 检查是否是系统消息
     *
     * @return 是否是系统消息
     */
    public boolean isSystem() {
        return type == MessageType.SYSTEM;
    }
} 