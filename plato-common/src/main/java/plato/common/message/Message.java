package plato.common.message;

import java.io.Serializable;

/**
 * 消息接口
 * <p>
 * 定义所有类型消息的通用接口，规范消息的基本行为
 * </p>
 */
public interface Message extends Serializable {
    
    /**
     * 获取消息类型
     * 
     * @return 消息类型
     */
    MessageType getType();
    
    /**
     * 获取消息ID
     * 
     * @return 消息唯一标识
     */
    String getMessageId();
    
    /**
     * 设置消息ID
     * 
     * @param messageId 消息唯一标识
     */
    void setMessageId(String messageId);
    
    /**
     * 获取发送时间戳
     * 
     * @return 发送时间戳（毫秒）
     */
    long getTimestamp();
    
    /**
     * 设置发送时间戳
     * 
     * @param timestamp 发送时间戳（毫秒）
     */
    void setTimestamp(long timestamp);
    
    /**
     * 序列化消息为字节数组
     * 
     * @return 序列化后的字节数组
     */
    byte[] serialize();
    
    /**
     * 克隆消息
     * 
     * @return 消息的深拷贝
     */
    Message clone();
    
    /**
     * 验证消息的有效性
     * 
     * @return 如果消息有效返回true，否则返回false
     */
    boolean isValid();
    
    /**
     * 获取消息优先级
     * 
     * @return 消息优先级，数值越小优先级越高
     */
    default int getPriority() {
        return 0; // 默认优先级
    }
    
    /**
     * 获取消息的目标接收者标识
     * 
     * @return 接收者标识
     */
    String getReceiverId();
    
    /**
     * 获取消息的发送者标识
     * 
     * @return 发送者标识
     */
    String getSenderId();
} 