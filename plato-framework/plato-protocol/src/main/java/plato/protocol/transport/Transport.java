package plato.protocol.transport;

import java.util.concurrent.CompletableFuture;

/**
 * 传输接口
 * <p>
 * 定义传输层的基本操作，包括连接、断开、发送和接收数据
 * </p>
 */
public interface Transport {

    /**
     * 连接到指定地址
     *
     * @param host 主机地址
     * @param port 端口
     * @throws TransportException 如果连接失败
     */
    void connect(String host, int port) throws TransportException;

    /**
     * 异步连接到指定地址
     *
     * @param host 主机地址
     * @param port 端口
     * @return 连接完成的Future
     */
    CompletableFuture<Void> connectAsync(String host, int port);

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 异步断开连接
     *
     * @return 断开连接完成的Future
     */
    CompletableFuture<Void> disconnectAsync();

    /**
     * 发送数据
     *
     * @param data 要发送的数据
     * @throws TransportException 如果发送失败
     */
    void send(byte[] data) throws TransportException;

    /**
     * 异步发送数据
     *
     * @param data 要发送的数据
     * @return 发送完成的Future
     */
    CompletableFuture<Void> sendAsync(byte[] data);

    /**
     * 接收数据
     *
     * @return 接收到的数据
     * @throws TransportException 如果接收失败
     */
    byte[] receive() throws TransportException;

    /**
     * 异步接收数据
     *
     * @return 接收到数据的Future
     */
    CompletableFuture<byte[]> receiveAsync();

    /**
     * 设置消息处理器
     *
     * @param handler 消息处理器
     */
    void setMessageHandler(MessageHandler handler);

    /**
     * 获取当前连接状态
     *
     * @return 连接状态
     */
    TransportState getState();

    /**
     * 是否已连接
     *
     * @return 是否已连接
     */
    boolean isConnected();

    /**
     * 获取传输配置
     *
     * @return 传输配置
     */
    TransportConfig getConfig();

    /**
     * 设置传输配置
     *
     * @param config 传输配置
     */
    void setConfig(TransportConfig config);

    /**
     * 消息处理器接口
     */
    @FunctionalInterface
    interface MessageHandler {
        /**
         * 处理接收到的消息
         *
         * @param data 消息数据
         */
        void onMessage(byte[] data);
    }

    /**
     * 传输状态枚举
     */
    enum TransportState {
        /**
         * 已初始化
         */
        INITIALIZED,

        /**
         * 正在连接
         */
        CONNECTING,

        /**
         * 已连接
         */
        CONNECTED,

        /**
         * 正在断开
         */
        DISCONNECTING,

        /**
         * 已断开
         */
        DISCONNECTED,

        /**
         * 已关闭
         */
        CLOSED,

        /**
         * 错误状态
         */
        ERROR
    }
}