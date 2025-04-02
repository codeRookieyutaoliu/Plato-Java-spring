package plato.common.net;

/**
 * 连接事件监听器接口
 * <p>
 * 定义了连接相关事件的监听处理，包括连接建立、关闭、异常等事件
 * 可用于在连接生命周期的各个阶段进行自定义处理
 * </p>
 */
public interface ConnectionListener {
    
    /**
     * 连接建立事件
     * 
     * @param connection 新建立的连接
     */
    void onConnectionEstablished(Connection connection);
    
    /**
     * 连接关闭事件
     * 
     * @param connection 已关闭的连接
     */
    void onConnectionClosed(Connection connection);
    
    /**
     * 连接异常事件
     * 
     * @param connection 发生异常的连接
     * @param cause 异常原因
     */
    void onConnectionError(Connection connection, Throwable cause);
    
    /**
     * 接收到消息事件
     * 
     * @param connection 接收消息的连接
     * @param message 接收到的消息
     */
    void onMessageReceived(Connection connection, byte[] message);
    
    /**
     * 消息发送成功事件
     * 
     * @param connection 发送消息的连接
     * @param messageId 消息ID
     */
    void onMessageSent(Connection connection, String messageId);
    
    /**
     * 消息发送失败事件
     * 
     * @param connection 发送消息的连接
     * @param messageId 消息ID
     * @param cause 失败原因
     */
    void onMessageSendFailed(Connection connection, String messageId, Throwable cause);
    
    /**
     * 空闲连接事件
     * 
     * @param connection 空闲的连接
     * @param idleTimeMillis 空闲时间(毫秒)
     */
    void onConnectionIdle(Connection connection, long idleTimeMillis);
} 