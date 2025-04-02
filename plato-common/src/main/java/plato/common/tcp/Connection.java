package plato.common.tcp;

import lombok.Builder;

import java.nio.ByteBuffer;

/**
 * TCP连接接口
 * 定义TCP连接的基本操作和事件处理
 */

public interface Connection {
    /**
     * 获取连接ID
     * @return 连接唯一标识
     */
    String getId();

    /**
     * 发送数据
     * @param data 要发送的数据
     * @return 是否发送成功
     */
    boolean send(ByteBuffer data);

    /**
     * 关闭连接
     */
    void close();

    /**
     * 设置连接事件处理器
     * @param handler 事件处理器
     */
    void setEventHandler(ConnectionEventHandler handler);

    /**
     * 连接事件处理器接口
     */
    interface ConnectionEventHandler {
        /**
         * 处理接收到的数据
         * @param conn 连接对象
         * @param data 接收到的数据
         */
        void onReceive(Connection conn, ByteBuffer data);

        /**
         * 处理连接关闭事件
         * @param conn 连接对象
         */
        void onClose(Connection conn);

        /**
         * 处理连接错误事件
         * @param conn 连接对象
         * @param error 错误信息
         */
        void onError(Connection conn, Throwable error);
    }
}