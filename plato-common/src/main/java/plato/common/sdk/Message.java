package plato.common.sdk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息类
 * 定义了消息的基本结构
 * 对应Go代码中的Message结构体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    /**
     * 消息类型
     * 对应Go代码中的Type字段
     */
    private String type;
    
    /**
     * 发送者名称
     * 对应Go代码中的Name字段
     */
    private String name;
    
    /**
     * 发送者用户ID
     * 对应Go代码中的FormUserID字段
     */
    private String fromUserId;
    
    /**
     * 接收者用户ID
     * 对应Go代码中的ToUserID字段
     */
    private String toUserId;
    
    /**
     * 消息内容
     * 对应Go代码中的Content字段
     */
    private String content;
    
    /**
     * 会话ID
     * 对应Go代码中的Session字段
     */
    private String sessionId;
    
    /**
     * 文本消息类型
     * 对应Go代码中的MsgTypeText常量
     */
    public static final String TYPE_TEXT = "text";
    
    /**
     * 确认消息类型
     * 对应Go代码中的MsgTypeAck常量
     */
    public static final String TYPE_ACK = "ack";
    
    /**
     * 重连消息类型
     * 对应Go代码中的MsgTypeReConn常量
     */
    public static final String TYPE_RECONNECT = "reConn";
    
    /**
     * 心跳消息类型
     * 对应Go代码中的MsgTypeHeartbeat常量
     */
    public static final String TYPE_HEARTBEAT = "heartbeat";
    
    /**
     * 登录消息类型
     * 对应Go代码中的MsgLogin常量
     */
    public static final String TYPE_LOGIN = "loginMsg";
} 