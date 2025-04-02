package plato.message.application.service.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import plato.message.application.api.MessageAPI;
import plato.message.application.dto.MessageDTO;
import plato.message.application.handler.MessageHandler;
import plato.message.application.registry.MessageHandlerRegistry;
import plato.message.application.service.MessageProcessingService;
import plato.message.domain.entity.Message;
import plato.message.domain.factory.MessageFactory;
import plato.message.infrastructure.codec.MessageCodec;
import plato.message.infrastructure.converter.MessageConverter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 消息API实现类
 * <p>
 * 实现消息模块对外的服务接口
 * </p>
 */
@Service
public class MessageAPIImpl implements MessageAPI {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageAPIImpl.class);
    
    /**
     * 消息处理服务
     */
    private final MessageProcessingService messageProcessingService;
    
    /**
     * 消息处理器注册表
     */
    private final MessageHandlerRegistry messageHandlerRegistry;
    
    /**
     * 默认消息处理器
     */
    private MessageHandler<?> defaultHandler;
    
    /**
     * 构造函数
     *
     * @param messageProcessingService 消息处理服务
     * @param messageHandlerRegistry 消息处理器注册表
     */
    @Autowired
    public MessageAPIImpl(MessageProcessingService messageProcessingService, MessageHandlerRegistry messageHandlerRegistry) {
        this.messageProcessingService = messageProcessingService;
        this.messageHandlerRegistry = messageHandlerRegistry;
    }
    
    /**
     * 发送消息
     *
     * @param messageDTO 要发送的消息DTO
     * @return 处理结果的Future
     */
    @Override
    public CompletableFuture<Boolean> sendMessage(MessageDTO messageDTO) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 转换DTO为领域模型
                Message message = MessageConverter.toDomain(messageDTO);
                if (message == null) {
                    LOGGER.error("发送消息失败：无法转换DTO为领域模型");
                    return false;
                }
                
                // 编码消息为字节数组
                byte[] bytes = MessageCodec.encode(message);
                
                // TODO: 实际发送消息的网络操作，将在网络模块中实现
                LOGGER.info("消息已编码，准备发送：{}, 字节数：{}", message, bytes.length);
                
                return true;
            } catch (Exception e) {
                LOGGER.error("发送消息时发生异常", e);
                return false;
            }
        });
    }
    
    /**
     * 处理接收到的字节数组消息
     *
     * @param bytes 接收到的字节数组
     * @return 处理结果的Future
     */
    @Override
    public CompletableFuture<Object> processMessage(byte[] bytes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 解码字节数组为领域模型
                Message message = MessageCodec.decode(bytes);
                
                // 处理消息
                return processMessage(message).join();
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error("解析消息失败", e);
                return null;
            }
        });
    }
    
    /**
     * 处理接收到的消息实体
     *
     * @param message 接收到的消息实体
     * @return 处理结果的Future
     */
    @Override
    public CompletableFuture<Object> processMessage(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 使用消息处理服务处理消息
                List<Object> results = messageProcessingService.processMessage(message).join();
                
                // 如果没有处理器处理，使用默认处理器
                if (results.isEmpty() && defaultHandler != null && defaultHandler.supports(message)) {
                    return defaultHandler.handle(message);
                }
                
                // 返回第一个处理结果
                return results.isEmpty() ? null : results.get(0);
            } catch (Exception e) {
                LOGGER.error("处理消息时发生异常", e);
                return null;
            }
        });
    }
    
    /**
     * 注册消息处理器
     *
     * @param handlerClass 处理器类
     */
    @Override
    public void registerHandler(Class<?> handlerClass) {
        try {
            if (MessageHandler.class.isAssignableFrom(handlerClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends MessageHandler<?>> messageHandlerClass = (Class<? extends MessageHandler<?>>) handlerClass;
                MessageHandler<?> handler = messageHandlerClass.getDeclaredConstructor().newInstance();
                messageProcessingService.registerHandler(handler);
                LOGGER.info("注册消息处理器：{}", handlerClass.getSimpleName());
            } else {
                LOGGER.error("注册消息处理器失败：类 {} 不是 MessageHandler 的实现", handlerClass.getName());
            }
        } catch (Exception e) {
            LOGGER.error("注册消息处理器时发生异常", e);
        }
    }
    
    /**
     * 取消注册消息处理器
     *
     * @param handlerClass 处理器类
     */
    @Override
    public void unregisterHandler(Class<?> handlerClass) {
        try {
            if (MessageHandler.class.isAssignableFrom(handlerClass)) {
                // 获取所有已注册的处理器
                List<MessageHandler<?>> handlers = messageHandlerRegistry.getHandlers();
                
                // 查找匹配的处理器并取消注册
                for (MessageHandler<?> handler : handlers) {
                    if (handler.getClass().equals(handlerClass)) {
                        messageProcessingService.unregisterHandler(handler);
                        LOGGER.info("取消注册消息处理器：{}", handlerClass.getSimpleName());
                        break;
                    }
                }
            } else {
                LOGGER.error("取消注册消息处理器失败：类 {} 不是 MessageHandler 的实现", handlerClass.getName());
            }
        } catch (Exception e) {
            LOGGER.error("取消注册消息处理器时发生异常", e);
        }
    }
    
    /**
     * 设置默认消息处理器
     *
     * @param handlerClass 默认处理器类
     */
    @Override
    public void setDefaultHandler(Class<?> handlerClass) {
        try {
            if (MessageHandler.class.isAssignableFrom(handlerClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends MessageHandler<?>> messageHandlerClass = (Class<? extends MessageHandler<?>>) handlerClass;
                defaultHandler = messageHandlerClass.getDeclaredConstructor().newInstance();
                LOGGER.info("设置默认消息处理器：{}", handlerClass.getSimpleName());
            } else {
                LOGGER.error("设置默认消息处理器失败：类 {} 不是 MessageHandler 的实现", handlerClass.getName());
            }
        } catch (Exception e) {
            LOGGER.error("设置默认消息处理器时发生异常", e);
        }
    }
}