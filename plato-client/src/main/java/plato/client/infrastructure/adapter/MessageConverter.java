package plato.client.infrastructure.adapter;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import lombok.extern.slf4j.Slf4j;
import plato.common.message.AckMessage;
import plato.common.message.CmdType;
import plato.common.message.MsgCmd;
import plato.common.sdk.Message;

/**
 * 消息转换器
 * 负责处理Protobuf消息和SDK消息之间的转换
 */
@Slf4j
public class MessageConverter {
    /**
     * 将SDK消息转换为Protobuf消息
     *
     * @param message SDK消息
     * @return Protobuf消息
     */
    public static MsgCmd toProtobuf(Message message) {
        MsgCmd.Builder builder = MsgCmd.newBuilder();
        
        // 只设置我们确定存在的字段
        switch (message.getType()) {
            case Message.TYPE_TEXT:
                builder.setType(CmdType.UP);
                // 设置消息内容
                if (message.getContent() != null) {
                    builder.setPayload(ByteString.copyFromUtf8(message.getContent()));
                }
                break;
            case Message.TYPE_HEARTBEAT:
                builder.setType(CmdType.Heartbeat);
                break;
            case Message.TYPE_LOGIN:
                builder.setType(CmdType.Login);
                break;
            case Message.TYPE_RECONNECT:
                builder.setType(CmdType.ReConn);
                break;
            default:
                log.warn("未知的消息类型: {}", message.getType());
                break;
        }
        
        return builder.build();
    }

    /**
     * 将Protobuf消息转换为SDK消息
     *
     * @param msgCmd Protobuf消息
     * @return SDK消息
     */
    public static Message fromProtobuf(MsgCmd msgCmd) {
        Message.MessageBuilder builder = Message.builder();

        // 只处理我们确定存在的字段和消息类型
        switch (msgCmd.getType()) {
            case ACK:
                builder.type(Message.TYPE_ACK);
                // 解析ACK消息
                try {
                    ByteString payload = msgCmd.getPayload();
                    if (payload != null && !payload.isEmpty()) {
                        ACKMsg ackMsg = ACKMsg.parseFrom(payload);
                        builder.content("Code: " + ackMsg.getCode() + ", Msg: " + ackMsg.getMsg());
                    }
                } catch (Exception e) {
                    log.error("解析ACK消息异常", e);
                }
                break;
            case Push:
                builder.type(Message.TYPE_TEXT);
                // 解析推送消息
                ByteString payload = msgCmd.getPayload();
                if (payload != null && !payload.isEmpty()) {
                    builder.content(payload.toStringUtf8());
                }
                break;
            case Heartbeat:
                builder.type(Message.TYPE_HEARTBEAT);
                break;
            default:
                log.warn("未知的消息类型: {}", msgCmd.getType());
                break;
        }
        
        return builder.build();
    }
    
    /**
     * 获取ACK消息中的连接ID
     *
     * @param msgCmd Protobuf消息
     * @return 连接ID，如果不存在则返回0
     */
    public static long getConnectionIdFromAck(MsgCmd msgCmd) {
        if (msgCmd.getType() == CmdType.ACK) {
            try {
                ByteString payload = msgCmd.getPayload();
                if (payload != null && !payload.isEmpty()) {
                    ACKMsg ackMsg = ACKMsg.parseFrom(payload);
                    if (ackMsg.getType() == CmdType.Login || ackMsg.getType() == CmdType.ReConn) {
                        return ackMsg.getConnId();
                    }
                }
            } catch (Exception e) {
                log.error("解析ACK消息异常", e);
            }
        }
        return 0;
    }
} 