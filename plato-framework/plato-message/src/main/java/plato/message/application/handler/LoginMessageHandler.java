package plato.message.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.domain.entity.AckMessage;
import plato.message.domain.entity.LoginMessage;
import plato.message.domain.factory.MessageFactory;
import plato.message.domain.valueobject.MessageType;

/**
 * 登录消息处理器
 * <p>
 * 处理客户端发送的登录消息
 * </p>
 */
public class LoginMessageHandler extends AbstractMessageHandler<LoginMessage> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginMessageHandler.class);
    
    /**
     * 获取此处理器支持的消息类型
     *
     * @return LoginMessage.class
     */
    @Override
    protected Class<LoginMessage> getMessageClass() {
        return LoginMessage.class;
    }
    
    /**
     * 获取此处理器支持的消息类型枚举值
     *
     * @return MessageType.LOGIN
     */
    @Override
    protected MessageType getSupportedMessageType() {
        return MessageType.LOGIN;
    }
    
    /**
     * 处理登录消息
     * <p>
     * 验证用户身份，建立连接，并返回确认消息
     * </p>
     *
     * @param message 登录消息
     * @return 确认消息
     */
    @Override
    public Object handle(LoginMessage message) {
        try {
            // 处理前的钩子方法
            preHandle(message);
            
            LOGGER.debug("处理登录消息: uid={}, deviceId={}", message.getUid(), message.getDeviceId());
            
            // TODO: 验证用户身份，建立连接
            
            // 创建确认消息
            AckMessage ackMessage = MessageFactory.createAckMessage(
                    0,  // 成功状态码
                    "登录成功",
                    MessageType.LOGIN,
                    0,  // 连接ID，在实际应用中应该是新建立的连接ID
                    0,  // 客户端ID，在实际应用中应该是分配的客户端ID
                    0,  // 会话ID，在实际应用中应该是新建立的会话ID
                    0   // 消息ID，在实际应用中应该是登录消息的ID
            );
            
            // 处理后的钩子方法
            postHandle(message, ackMessage);
            
            return ackMessage;
        } catch (Exception e) {
            LOGGER.error("处理登录消息时发生异常", e);
            handleException(message, e);
            
            // 创建错误确认消息
            return MessageFactory.createAckMessage(
                    500,  // 错误状态码
                    "处理登录消息失败: " + e.getMessage(),
                    MessageType.LOGIN,
                    0,  // 假设连接ID未知，实际应用中应从上下文获取
                    0,  // 假设客户端ID未知，实际应用中应从上下文获取
                    0,  // 假设会话ID未知，实际应用中应从上下文获取
                    0   // 假设消息ID未知，实际应用中应从上下文获取
            );
        }
    }
}