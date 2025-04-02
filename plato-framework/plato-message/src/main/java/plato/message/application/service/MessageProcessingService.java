package plato.message.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.application.handler.MessageHandler;
import plato.message.application.registry.MessageHandlerRegistry;
import plato.message.domain.entity.Message;
import plato.message.domain.factory.MessageFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 消息处理服务
 * <p>
 * 用于处理接收到的消息，并将消息分发到对应的处理器
 * </p>
 */
public class MessageProcessingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessingService.class);
    
    /**
     * 消息处理器注册表
     */
    private final MessageHandlerRegistry registry;
    
    /**
     * 消息处理线程池
     */
    private final Executor executor;
    
    /**
     * 构造函数
     *
     * @param registry 消息处理器注册表
     */
    public MessageProcessingService(MessageHandlerRegistry registry) {
        this(registry, Executors.newVirtualThreadPerTaskExecutor());
    }
    
    /**
     * 构造函数
     *
     * @param registry 消息处理器注册表
     * @param executor 消息处理线程池
     */
    public MessageProcessingService(MessageHandlerRegistry registry, Executor executor) {
        this.registry = registry;
        this.executor = executor;
    }
    
    /**
     * 处理接收到的消息
     *
     * @param message 要处理的消息
     * @return 处理结果的Future
     */
    public CompletableFuture<List<Object>> processMessage(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<MessageHandler<Message>> handlers = registry.findHandlers(message);
                return handlers.stream()
                        .map(handler -> {
                            try {
                                return handler.handle(message);
                            } catch (Exception e) {
                                LOGGER.error("处理消息时发生异常", e);
                                return null;
                            }
                        })
                        .filter(result -> result != null)
                        .toList();
            } catch (Exception e) {
                LOGGER.error("查找消息处理器时发生异常", e);
                throw e;
            }
        }, executor);
    }
    
    /**
     * 处理接收到的Protocol Buffers消息
     *
     * @param bytes 要处理的Protocol Buffers消息字节数组
     * @return 处理结果的Future
     */
    public CompletableFuture<List<Object>> processProtoMessage(byte[] bytes) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Message message = MessageFactory.createFromProtoBytes(bytes);
                return processMessage(message).join();
            } catch (Exception e) {
                LOGGER.error("解析消息时发生异常", e);
                throw new RuntimeException("解析消息失败", e);
            }
        }, executor);
    }
    
    /**
     * 注册消息处理器
     *
     * @param handler 要注册的消息处理器
     */
    public void registerHandler(MessageHandler<?> handler) {
        registry.register(handler);
    }
    
    /**
     * 注册消息处理器并指定消息类型
     *
     * @param type 消息类型
     * @param handler 要注册的消息处理器
     */
    public void registerHandler(plato.message.domain.valueobject.MessageType type, MessageHandler<?> handler) {
        registry.register(type, handler);
    }
    
    /**
     * 取消注册消息处理器
     *
     * @param handler 要取消注册的消息处理器
     */
    public void unregisterHandler(MessageHandler<?> handler) {
        registry.unregister(handler);
    }
} 