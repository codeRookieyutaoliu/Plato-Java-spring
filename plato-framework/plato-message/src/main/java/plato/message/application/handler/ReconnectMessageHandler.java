package plato.message.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import plato.common.exception.BusinessException;
import plato.message.domain.entity.AckMessage;
import plato.message.domain.entity.ReconnectMessage;
import plato.message.domain.factory.MessageFactory;
import plato.message.domain.valueobject.MessageType;

/**
 * 重连消息处理器
 * <p>
 * 处理客户端的重连请求，恢复连接状态
 * </p>
 * 
 * 对应Go项目中的 reConnMsgHandler 函数
 */
@Component
public class ReconnectMessageHandler extends AbstractMessageHandler<ReconnectMessage> {
    
    private static final Logger logger = LoggerFactory.getLogger(ReconnectMessageHandler.class);

    /**
     * 成功状态码
     */
    private static final int SUCCESS_CODE = 0;
    
    /**
     * 失败状态码
     */
    private static final int FAILURE_CODE = 1;
    
    @Override
    protected Class<ReconnectMessage> getMessageClass() {
        return ReconnectMessage.class;
    }
    
    @Override
    protected MessageType getSupportedMessageType() {
        return MessageType.RECONNECT;
    }
    
    @Override
    public Object handle(ReconnectMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("重连消息不能为空");
        }
        
        logger.info("处理重连消息: uid={}, deviceId={}, connId={}", 
                message.getUid(), message.getDeviceId(), message.getConnId());
        
        try {
            // 执行重连操作
            boolean reconnectResult = reconnect(message);
            
            if (reconnectResult) {
                logger.info("重连成功: connId={}", message.getConnId());
                // 创建成功的ACK消息
                return MessageFactory.createAckMessage(
                        SUCCESS_CODE,
                        "reconnect success",
                        MessageType.RECONNECT,
                        message.getConnId(),
                        0,
                        0,
                        0
                );
            } else {
                logger.warn("重连失败: connId={}", message.getConnId());
                // 创建失败的ACK消息
                return MessageFactory.createAckMessage(
                        FAILURE_CODE,
                        "reconnect failed",
                        MessageType.RECONNECT,
                        message.getConnId(),
                        0,
                        0,
                        0
                );
            }
        } catch (Exception e) {
            logger.error("重连过程发生异常: connId={}, error={}", message.getConnId(), e.getMessage(), e);
            throw new BusinessException("重连失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行重连操作
     * <p>
     * 根据旧的连接ID恢复连接状态，将状态绑定到新的连接ID
     * </p>
     * 
     * @param message 重连消息
     * @return 重连是否成功
     */
    private boolean reconnect(ReconnectMessage message) {
        // TODO: 这里需要调用连接状态服务来处理重连逻辑
        // 在实际实现中，这里应该：
        // 1. 根据旧的connId查找连接状态
        // 2. 如果找到状态，将其与新的连接ID绑定
        // 3. 重置心跳计时器
        // 4. 如果需要，重发未确认的消息
        
        // 模拟实现，始终返回成功
        return true;
    }
    
    @Override
    protected void preHandle(ReconnectMessage message) {
        logger.debug("开始处理重连消息: connId={}", message.getConnId());
    }
    
    @Override
    protected void postHandle(ReconnectMessage message, Object result) {
        if (result instanceof AckMessage) {
            AckMessage ackMessage = (AckMessage) result;
            logger.debug("重连消息处理完成: connId={}, code={}, msg={}", 
                    message.getConnId(), ackMessage.getCode(), ackMessage.getMsg());
        }
    }
    
    @Override
    protected void handleException(ReconnectMessage message, Exception exception) {
        logger.error("处理重连消息时发生异常: connId={}", message.getConnId(), exception);
    }
} 