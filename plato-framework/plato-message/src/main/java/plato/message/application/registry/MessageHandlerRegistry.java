package plato.message.application.registry;

import plato.message.application.handler.MessageHandler;
import plato.message.domain.entity.Message;
import plato.message.domain.valueobject.MessageType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消息处理器注册表
 * <p>
 * 用于管理所有消息处理器，支持按消息类型查找处理器
 * </p>
 */
public class MessageHandlerRegistry {
    
    /**
     * 按消息类型分组的处理器列表
     */
    private final Map<MessageType, List<MessageHandler<?>>> handlersByType = new EnumMap<>(MessageType.class);
    
    /**
     * 所有注册的处理器列表
     */
    private final List<MessageHandler<?>> handlers = new CopyOnWriteArrayList<>();
    
    /**
     * 注册消息处理器
     *
     * @param handler 要注册的消息处理器
     */
    public void register(MessageHandler<?> handler) {
        handlers.add(handler);
    }
    
    /**
     * 注册消息处理器并指定消息类型
     *
     * @param type 消息类型
     * @param handler 要注册的消息处理器
     */
    public void register(MessageType type, MessageHandler<?> handler) {
        handlersByType.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
        handlers.add(handler);
    }
    
    /**
     * 查找能处理指定消息的处理器
     *
     * @param message 要处理的消息
     * @return 能处理该消息的处理器列表
     */
    @SuppressWarnings("unchecked")
    public List<MessageHandler<Message>> findHandlers(Message message) {
        List<MessageHandler<Message>> result = new ArrayList<>();
        
        // 先查找按类型注册的处理器
        List<MessageHandler<?>> typeHandlers = handlersByType.get(message.getType());
        if (typeHandlers != null) {
            for (MessageHandler<?> handler : typeHandlers) {
                if (handler.supports(message)) {
                    result.add((MessageHandler<Message>) handler);
                }
            }
        }
        
        // 再查找通过supports方法判断的处理器
        for (MessageHandler<?> handler : handlers) {
            if (handler.supports(message) && !result.contains(handler)) {
                result.add((MessageHandler<Message>) handler);
            }
        }
        
        return result;
    }
    
    /**
     * 取消注册消息处理器
     *
     * @param handler 要取消注册的消息处理器
     */
    public void unregister(MessageHandler<?> handler) {
        handlers.remove(handler);
        
        // 从按类型分组的处理器中移除
        for (List<MessageHandler<?>> typeHandlers : handlersByType.values()) {
            typeHandlers.remove(handler);
        }
    }
    
    /**
     * 获取所有注册的处理器
     *
     * @return 所有处理器的列表
     */
    public List<MessageHandler<?>> getHandlers() {
        return new ArrayList<>(handlers);
    }
    
    /**
     * 获取指定类型的所有处理器
     *
     * @param type 消息类型
     * @return 该类型的所有处理器列表
     */
    public List<MessageHandler<?>> getHandlersByType(MessageType type) {
        List<MessageHandler<?>> typeHandlers = handlersByType.get(type);
        return typeHandlers != null ? new ArrayList<>(typeHandlers) : new ArrayList<>();
    }
    
    /**
     * 清空所有注册的处理器
     */
    public void clear() {
        handlers.clear();
        handlersByType.clear();
    }
} 