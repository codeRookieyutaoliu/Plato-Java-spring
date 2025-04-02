package plato.message.domain.entity;

import plato.message.domain.valueobject.MessageType;

/**
 * 消息接口
 * <p>
 * 定义所有消息类型必须实现的基本方法
 * </p>
 */
public interface Message {
    
    /**
     * 获取消息ID
     * 
     * @return 消息ID
     */
    String getId();
    
    /**
     * 获取消息类型
     * 
     * @return 消息类型
     */
    MessageType getType();
    
    /**
     * 将消息转换为字节数组
     * 
     * @return 消息的字节数组表示
     */
    byte[] toBytes();
    
    /**
     * 获取消息的时间戳
     * 
     * @return 消息时间戳（毫秒）
     */
    long getTimestamp();
} 