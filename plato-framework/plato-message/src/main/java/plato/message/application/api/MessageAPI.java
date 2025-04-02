package plato.message.application.api;

import java.util.concurrent.CompletableFuture;

import plato.message.application.dto.MessageDTO;
import plato.message.domain.entity.Message;

/**
 * 消息API接口
 * <p>
 * 提供消息模块对外的服务接口
 * </p>
 */
public interface MessageAPI {
    
    /**
     * 发送消息
     *
     * @param messageDTO 要发送的消息DTO
     * @return 处理结果的Future
     */
    CompletableFuture<Boolean> sendMessage(MessageDTO messageDTO);
    
    /**
     * 处理接收到的字节数组消息
     *
     * @param bytes 接收到的字节数组
     * @return 处理结果的Future
     */
    CompletableFuture<Object> processMessage(byte[] bytes);
    
    /**
     * 处理接收到的消息实体
     *
     * @param message 接收到的消息实体
     * @return 处理结果的Future
     */
    CompletableFuture<Object> processMessage(Message message);
    
    /**
     * 注册消息处理器
     *
     * @param handlerClass 处理器类
     */
    void registerHandler(Class<?> handlerClass);
    
    /**
     * 取消注册消息处理器
     *
     * @param handlerClass 处理器类
     */
    void unregisterHandler(Class<?> handlerClass);
    
    /**
     * 设置默认消息处理器
     * <p>
     * 当没有找到匹配的处理器时，使用默认处理器处理消息
     * </p>
     *
     * @param handlerClass 默认处理器类
     */
    void setDefaultHandler(Class<?> handlerClass);
}