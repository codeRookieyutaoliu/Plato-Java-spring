package plato.message.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.domain.entity.AckMessage;
import plato.message.domain.entity.HeartbeatMessage;
import plato.message.domain.entity.Message;
import plato.message.domain.factory.MessageFactory;
import plato.message.domain.valueobject.MessageType;

/**
 * 心跳消息处理器
 * <p>
 * 处理客户端发送的心跳消息
 * </p>
 */
public class HeartbeatMessageHandler extends AbstractMessageHandler<HeartbeatMessage> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatMessageHandler.class);
    
    /**
     * 获取此处理器支持的消息类型
     *
     * @return HeartbeatMessage.class
     */
    @Override
    protected Class<HeartbeatMessage> getMessageClass() {
        return HeartbeatMessage.class;
    }
    
    /**
     * 获取此处理器支持的消息类型枚举值
     *
     * @return MessageType.HEARTBEAT
     */
    @Override
    protected MessageType getSupportedMessageType() {
        return MessageType.HEARTBEAT;
    }
    
    /**
     * 处理心跳消息
     * <p>
     * 更新连接最后活跃时间，并返回确认消息
     * </p>
     *
     * @param message 心跳消息
     * @return 确认消息
     */
    @Override
    public Object handle(HeartbeatMessage message) {
        try {
            // 处理前的钩子方法
            preHandle(message);
            
            LOGGER.debug("处理心跳消息: uid={}, deviceId={}", message.getUid(), message.getDeviceId());
            
            // 创建确认消息
            AckMessage ackMessage = MessageFactory.createAckMessage(
                    0,  // 成功状态码
                    "心跳成功",
                    MessageType.HEARTBEAT,
                    0,  // 假设连接ID未知，实际应用中应从上下文获取
                    0,  // 假设客户端ID未知，实际应用中应从上下文获取
                    0,  // 假设会话ID未知，实际应用中应从上下文获取
                    0   // 假设消息ID未知，实际应用中应从上下文获取
            );
            
            // 处理后的钩子方法
            postHandle(message, ackMessage);
            
            return ackMessage;
        } catch (Exception e) {
            LOGGER.error("处理心跳消息时发生异常", e);
            handleException(message, e);
            
            // 创建错误确认消息
            return MessageFactory.createAckMessage(
                    500,  // 错误状态码
                    "处理心跳消息失败: " + e.getMessage(),
                    MessageType.HEARTBEAT,
                    0,  // 假设连接ID未知，实际应用中应从上下文获取
                    0,  // 假设客户端ID未知，实际应用中应从上下文获取
                    0,  // 假设会话ID未知，实际应用中应从上下文获取
                    0   // 假设消息ID未知，实际应用中应从上下文获取
            );
        }
    }
}