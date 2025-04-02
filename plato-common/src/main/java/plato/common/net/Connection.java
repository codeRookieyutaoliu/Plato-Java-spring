package plato.common.net;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * 网络连接接口
 * <p>
 * 定义了所有连接类型的通用接口，包含连接的基本操作和事件处理
 * 可应用于TCP、WebSocket等多种连接类型
 * </p>
 */
public interface Connection {
    
    /**
     * 获取连接ID
     * 
     * @return 连接唯一标识
     */
    String getId();
    
    /**
     * 获取远程地址
     * 
     * @return 远程地址字符串
     */
    String getRemoteAddress();
    
    /**
     * 获取连接创建时间
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
     * 更新最后活动时间为当前时间
     */
    void updateLastActiveTime();
    
    /**
     * 检查连接是否活跃
     * 
     * @return 如果连接处于活跃状态，返回true
     */
    boolean isActive();
    
    /**
     * 同步发送数据
     * 
     * @param data 要发送的数据
     * @return 是否发送成功
     */
    boolean send(ByteBuffer data);
    
    /**
     * 异步发送数据
     * 
     * @param data 要发送的数据
     * @return 发送结果的Future
     */
    CompletableFuture<Boolean> sendAsync(ByteBuffer data);
    
    /**
     * 关闭连接
     */
    void close();
    
    /**
     * 设置连接事件处理器
     * 
     * @param handler 事件处理器
     */
    void setEventHandler(ConnectionEventHandler handler);
    
    /**
     * 连接事件处理器接口
     */
    interface ConnectionEventHandler {
        /**
         * 处理接收到的数据
         * 
         * @param conn 连接对象
         * @param data 接收到的数据
         */
        void onReceive(Connection conn, ByteBuffer data);
        
        /**
         * 处理连接关闭事件
         * 
         * @param conn 连接对象
         */
        void onClose(Connection conn);
        
        /**
         * 处理连接错误事件
         * 
         * @param conn 连接对象
         * @param error 错误信息
         */
        void onError(Connection conn, Throwable error);
    }
} 