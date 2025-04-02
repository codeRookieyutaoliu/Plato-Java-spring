package plato.gateway.infrastructure.connection;

import java.time.Instant;

/**
 * 连接接口
 * 定义连接的通用方法，用于适配不同的连接实现
 */
public interface IConnection {
    
    /**
     * 获取连接ID
     *
     * @return 连接ID
     */
    long getId();
    
    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    String getRemoteAddress();
    
    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    Instant getCreateTime();
    
    /**
     * 获取最后活动时间
     *
     * @return 最后活动时间
     */
    Instant getLastActiveTime();
    
    /**
     * 更新最后活动时间
     */
    void updateLastActiveTime();
    
    /**
     * 检查是否空闲超时
     *
     * @param maxIdleTimeSeconds 最大空闲时间（秒）
     * @return 是否空闲超时
     */
    boolean isIdleTimeout(int maxIdleTimeSeconds);
    
    /**
     * 关闭连接
     */
    void close();
    
    /**
     * 发送消息
     *
     * @param payload 消息内容
     * @return 是否发送成功
     */
    boolean sendMessage(byte[] payload);
    
    /**
     * 接收消息回调
     *
     * @param data 接收到的数据
     */
    void onReceive(byte[] data);
    
    /**
     * 获取接收的字节数
     *
     * @return 接收的字节数
     */
    long getReceivedBytes();
    
    /**
     * 获取发送的字节数
     *
     * @return 发送的字节数
     */
    long getSentBytes();
} 