package plato.common.sdk;

import java.util.function.Consumer;

/**
 * 聊天连接接口
 * 定义了网络连接的基本操作
 * 对应Go代码中的connect结构体
 */
public interface ChatConnection {
    /**
     * 连接到服务器
     * 
     * @return 是否连接成功
     */
    boolean connect();
    
    /**
     * 发送消息
     * 
     * @param message 要发送的消息
     * @return 是否发送成功
     */
    boolean send(Message message);
    
    /**
     * 接收消息
     * 
     * @param messageConsumer 消息处理回调
     */
    void receive(Consumer<Message> messageConsumer);
    
    /**
     * 关闭连接
     */
    void close();
    
    /**
     * 获取连接ID
     * 
     * @return 连接ID
     */
    long getConnectionId();
    
    /**
     * 设置连接ID
     * 
     * @param connectionId 连接ID
     */
    void setConnectionId(long connectionId);
    
    /**
     * 检查是否已连接
     * 
     * @return 是否已连接
     */
    boolean isConnected();
} 