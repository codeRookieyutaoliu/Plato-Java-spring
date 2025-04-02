package plato.state.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息实体
 * 表示用户之间的消息
 * 对应Go中的消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    /**
     * 消息ID
     * 对应Go中的消息ID
     */
    private Long id;
    
    /**
     * 发送者ID
     * 对应Go中的发送者ID
     */
    private Long fromUserId;
    
    /**
     * 接收者ID
     * 对应Go中的接收者ID
     */
    private Long toUserId;
    
    /**
     * 消息内容
     * 对应Go中的消息内容
     */
    private byte[] payload;
    
    /**
     * 发送时间
     * 对应Go中的发送时间
     */
    private LocalDateTime sentTime;
    
    /**
     * 消息状态
     * 对应Go中的消息状态
     */
    private MessageStatus status;
    
    /**
     * 创建新消息
     * 对应Go中的创建消息方法
     *
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @param payload    消息内容
     * @return 新消息
     */
    public static Message create(Long fromUserId, Long toUserId, byte[] payload) {
        // 创建新消息
        // 对应Go中的创建消息逻辑
        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setPayload(payload);
        message.setSentTime(LocalDateTime.now());
        message.setStatus(MessageStatus.SENT);
        return message;
    }
    
    /**
     * 标记为已送达
     * 对应Go中的标记送达方法
     */
    public void markAsDelivered() {
        // 标记为已送达
        // 对应Go中的标记送达逻辑
        this.status = MessageStatus.DELIVERED;
    }
    
    /**
     * 标记为已读
     * 对应Go中的标记已读方法
     */
    public void markAsRead() {
        // 标记为已读
        // 对应Go中的标记已读逻辑
        this.status = MessageStatus.READ;
    }
    
    /**
     * 消息状态枚举
     * 对应Go中的消息状态枚举
     */
    public enum MessageStatus {
        /**
         * 已发送
         * 对应Go中的已发送状态
         */
        SENT,
        
        /**
         * 已送达
         * 对应Go中的已送达状态
         */
        DELIVERED,
        
        /**
         * 已读
         * 对应Go中的已读状态
         */
        READ
    }
} 