package plato.protocol.codec.udp;

import java.net.InetSocketAddress;

/**
 * UDP会话接口
 * <p>
 * 管理UDP连接的状态，包括序列号、确认等
 * </p>
 */
public interface UdpSession {
    
    /**
     * 获取会话ID
     *
     * @return 会话ID
     */
    long getSessionId();
    
    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    InetSocketAddress getRemoteAddress();
    
    /**
     * 生成下一个发送序列号
     *
     * @return 序列号
     */
    int nextSequenceId();
    
    /**
     * 获取当前接收序列号
     *
     * @return 当前接收序列号
     */
    int getCurrentReceiveSequenceId();
    
    /**
     * 更新接收序列号
     *
     * @param sequenceId 接收的序列号
     * @return 是否更新成功（是否是新的序列号）
     */
    boolean updateReceiveSequenceId(int sequenceId);
    
    /**
     * 是否已经收到确认
     *
     * @param sequenceId 序列号
     * @return 是否已收到确认
     */
    boolean isAcknowledged(int sequenceId);
    
    /**
     * 标记已收到确认
     *
     * @param sequenceId 序列号
     */
    void acknowledge(int sequenceId);
    
    /**
     * 获取最后活动时间
     *
     * @return 最后活动时间（毫秒）
     */
    long getLastActiveTime();
    
    /**
     * 更新活动时间
     */
    void updateActiveTime();
    
    /**
     * 获取发送的字节数
     *
     * @return 发送字节数
     */
    long getSentBytes();
    
    /**
     * 获取接收的字节数
     *
     * @return 接收字节数
     */
    long getReceivedBytes();
    
    /**
     * 增加发送字节数
     *
     * @param bytes 增加的字节数
     */
    void addSentBytes(int bytes);
    
    /**
     * 增加接收字节数
     *
     * @param bytes 增加的字节数
     */
    void addReceivedBytes(int bytes);
    
    /**
     * 会话是否有效（未超时）
     *
     * @param timeoutMillis 超时时间（毫秒）
     * @return 是否有效
     */
    boolean isValid(long timeoutMillis);
    
    /**
     * 关闭会话
     */
    void close();
    
    /**
     * 会话是否已关闭
     *
     * @return 是否已关闭
     */
    boolean isClosed();
} 