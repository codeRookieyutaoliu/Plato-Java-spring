package plato.message.application.handler;

import plato.message.domain.entity.Message;
import plato.message.domain.valueobject.MessageType;

/**
 * 抽象消息处理器
 * <p>
 * 提供消息处理器的基础实现，简化具体处理器的开发
 * </p>
 *
 * @param <T> 消息类型
 */
public abstract class AbstractMessageHandler<T extends Message> implements MessageHandler<T> {
    
    /**
     * 获取此处理器支持的消息类型
     * 
     * @return 消息类型
     */
    protected abstract Class<T> getMessageClass();
    
    /**
     * 获取此处理器支持的消息类型枚举值
     * 如果处理器不限制消息类型，可以返回null
     * 
     * @return 消息类型枚举值，如果不限制类型则返回null
     */
    protected MessageType getSupportedMessageType() {
        return null;
    }
    
    /**
     * 判断该处理器是否支持处理指定的消息
     * 默认实现会检查消息的类型和类是否匹配
     *
     * @param message 要判断的消息
     * @return 如果支持处理该消息则返回true，否则返回false
     */
    @Override
    public boolean supports(Message message) {
        if (message == null) {
            return false;
        }
        
        // 检查消息类型是否匹配
        MessageType supportedType = getSupportedMessageType();
        if (supportedType != null && message.getType() != supportedType) {
            return false;
        }
        
        // 检查消息类是否匹配
        Class<T> messageClass = getMessageClass();
        return messageClass.isInstance(message);
    }
    
    /**
     * 处理前的钩子方法
     * 
     * @param message 要处理的消息
     */
    protected void preHandle(T message) {
        // 默认实现为空，子类可以根据需要覆盖
    }
    
    /**
     * 处理后的钩子方法
     * 
     * @param message 处理的消息
     * @param result 处理结果
     */
    protected void postHandle(T message, Object result) {
        // 默认实现为空，子类可以根据需要覆盖
    }
    
    /**
     * 处理消息出现异常时的钩子方法
     * 
     * @param message 处理的消息
     * @param exception 发生的异常
     */
    protected void handleException(T message, Exception exception) {
        // 默认实现为空，子类可以根据需要覆盖
    }
}