package plato.gateway.application.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import plato.gateway.domain.model.Message;

import java.time.Instant;

/**
 * 消息数据传输对象
 * 用于在应用层之间传递消息信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    /**
     * 消息ID
     */
    private long id;

    /**
     * 连接ID
     */
    private long connectionId;

    /**
     * 消息类型
     */
    private Message.MessageType type;

    /**
     * 消息内容
     */
    private byte[] payload;

    /**
     * 创建时间
     */
    private Instant createTime;

    /**
     * 消息大小
     */
    private int size;

    /**
     * 从领域模型创建DTO
     *
     * @param message 消息领域模型
     * @return 消息DTO
     */
    public static MessageDTO fromDomain(Message message) {
        return MessageDTO.builder()
            .id(message.getId())
            .connectionId(message.getConnectionId())
            .type(message.getType())
            .payload(message.getPayload())
            .createTime(message.getCreateTime())
            .size(message.getSize())
            .build();
    }
} 