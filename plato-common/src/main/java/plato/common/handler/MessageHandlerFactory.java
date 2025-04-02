package plato.common.handler;

import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plato.common.message.CmdType;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息处理器工厂
 * 用于管理和获取不同类型的消息处理器
 */
@Slf4j
@Component
public class MessageHandlerFactory {
    
    // 消息处理器映射表
    private final Map<CmdType, MessageHandler<? extends Message>> handlerMap = new HashMap<>();
    
    // 所有消息处理器
    private final List<MessageHandler<? extends Message>> handlers;
    
    /**
     * 构造函数
     *
     * @param handlers 所有消息处理器
     */
    @Autowired
    public MessageHandlerFactory(List<MessageHandler<? extends Message>> handlers) {
        this.handlers = handlers;
    }
    
    /**
     * 初始化
     * 注册所有消息处理器
     */
    @PostConstruct
    public void init() {
        for (MessageHandler<? extends Message> handler : handlers) {
            CmdType type = handler.getType();
            handlerMap.put(type, handler);
            log.info("注册消息处理器: type={}, handler={}", type, handler.getClass().getSimpleName());
        }
    }
    
    /**
     * 获取消息处理器
     *
     * @param type 消息类型
     * @return 消息处理器
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> MessageHandler<T> getHandler(CmdType type) {
        MessageHandler<? extends Message> handler = handlerMap.get(type);
        if (handler == null) {
            log.warn("未找到消息处理器: type={}", type);
            return null;
        }
        return (MessageHandler<T>) handler;
    }
} 