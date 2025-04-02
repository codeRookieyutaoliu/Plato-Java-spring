package plato.common.handler;

import com.google.protobuf.Message;
import plato.common.message.CmdType;

/**
 * 消息处理器接口
 * 用于处理不同类型的消息
 */
public interface MessageHandler<T extends Message> {
    
    /**
     * 获取处理的消息类型
     *
     * @return 消息类型
     */
    CmdType getType();
    
    /**
     * 获取处理的消息类
     *
     * @return 消息类
     */
    Class<T> getMessageClass();
    
    /**
     * 处理消息
     *
     * @param connectionId 连接ID
     * @param message 消息对象
     * @return 处理结果
     */
    boolean handle(long connectionId, T message);
} 