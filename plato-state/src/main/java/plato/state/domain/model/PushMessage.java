package plato.state.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推送消息
 * 表示一条推送给客户端的消息
 * 对应Go版本中的message.PushMsg结构体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushMessage {

    /**
     * 消息内容
     * 对应Go版本中的Content
     */
    private byte[] content;

    /**
     * 消息ID
     * 对应Go版本中的MsgID
     */
    private long msgId;

    /**
     * 会话ID
     * 对应Go版本中的SessionID
     */
    private long sessionId;
} 