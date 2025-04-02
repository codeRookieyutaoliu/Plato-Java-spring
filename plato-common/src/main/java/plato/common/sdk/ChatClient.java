package plato.common.sdk;

import java.util.function.Consumer;

/**
 * 聊天客户端接口
 * 定义了IM客户端的核心功能
 * 对应Go代码中的Chat结构体
 */
public interface ChatClient {
    /**
     * 连接到服务器
     * 
     * @return 是否连接成功
     */
    boolean connect();
    
    /**
     * 重新连接到服务器
     * 对应Go代码中的ReConn方法
     * 
     * @return 是否重连成功
     */
    boolean reconnect();
    
    /**
     * 关闭连接
     */
    void close();
    
    /**
     * 发送消息
     * 对应Go代码中的Send方法
     * 
     * @param message 要发送的消息
     * @return 是否发送成功
     */
    boolean send(Message message);
    
    /**
     * 接收消息
     * 对应Go代码中的Recv方法
     * 
     * @param messageConsumer 消息处理回调
     */
    void receive(Consumer<Message> messageConsumer);
    
    /**
     * 获取当前客户端ID
     * 对应Go代码中的GetCurClientID方法
     * 
     * @return 当前客户端ID
     */
    long getCurrentClientId();
    
    /**
     * 获取连接ID
     * 
     * @return 连接ID
     */
    long getConnectionId();
    
    /**
     * 检查是否已连接
     * 
     * @return 是否已连接
     */
    boolean isConnected();
} 