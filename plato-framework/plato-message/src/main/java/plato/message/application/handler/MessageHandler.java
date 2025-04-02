package plato.message.application.handler;

import plato.message.domain.entity.Message;

/**
 * 消息处理器接口
 * <p>
 * 定义消息处理器的基本行为
 * </p>
 *
 * @param <T> 消息类型
 */
public interface MessageHandler<T extends Message> {
    
    /**
     * 处理消息
     *
     * @param message 要处理的消息
     * @return 处理结果，可能是响应消息或者处理状态
     */
    Object handle(T message);
    
    /**
     * 判断该处理器是否支持处理指定的消息
     *
     * @param message 要判断的消息
     * @return 如果支持处理该消息则返回true，否则返回false
     */
    boolean supports(Message message);
} 