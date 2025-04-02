package plato.gateway.infrastructure.server;

/**
 * TCP 服务器接口
 * 定义服务器的通用方法，用于适配不同的服务器实现
 */
public interface ITcpServer {
    
    /**
     * 初始化服务器
     *
     * @throws Exception 如果初始化失败
     */
    void init() throws Exception;
    
    /**
     * 启动服务器
     */
    void start();
    
    /**
     * 停止服务器
     */
    void stop();
    
    /**
     * 关闭连接
     *
     * @param connectionId 连接ID
     * @return 是否成功
     */
    boolean closeConnection(long connectionId);
    
    /**
     * 发送消息
     *
     * @param connectionId 连接ID
     * @param payload 消息内容
     * @return 是否成功
     */
    boolean sendMessage(long connectionId, byte[] payload);
    
    /**
     * 获取连接数量
     *
     * @return 连接数量
     */
    int getConnectionCount();
} 