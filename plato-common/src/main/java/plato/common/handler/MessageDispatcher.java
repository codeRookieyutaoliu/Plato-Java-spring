package plato.common.handler;

import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plato.common.codec.MessageCodec;
import plato.common.message.CmdType;
import plato.common.message.MsgCmd;

/**
 * 消息分发器
 * 用于分发消息到对应的处理器
 */
@Slf4j
@Component
public class MessageDispatcher {
    
    // 消息处理器工厂
    private final MessageHandlerFactory handlerFactory;
    
    // 消息编解码器
    private final MessageCodec messageCodec;
    
    /**
     * 构造函数
     *
     * @param handlerFactory 消息处理器工厂
     * @param messageCodec 消息编解码器
     */
    @Autowired
    public MessageDispatcher(MessageHandlerFactory handlerFactory, MessageCodec messageCodec) {
        this.handlerFactory = handlerFactory;
        this.messageCodec = messageCodec;
    }
    
    /**
     * 分发消息
     *
     * @param connectionId 连接ID
     * @param data 消息数据
     * @return 处理结果
     */
    public boolean dispatch(long connectionId, byte[] data) {
        try {
            // 解码顶层消息
            MsgCmd msgCmd = messageCodec.decode(data, MsgCmd.class);
            
            // 获取消息类型
            CmdType type = msgCmd.getType();
            
            // 获取消息处理器
            MessageHandler<Message> handler = handlerFactory.getHandler(type);
            if (handler == null) {
                log.warn("未找到消息处理器: type={}", type);
                return false;
            }
            
            // 解码消息负载
            Message message = messageCodec.decode(msgCmd.getPayload().toByteArray(), handler.getMessageClass());
            
            // 处理消息
            return handler.handle(connectionId, message);
        } catch (Exception e) {
            log.error("分发消息失败: connectionId={}, error={}", connectionId, e.getMessage());
            return false;
        }
    }
} 