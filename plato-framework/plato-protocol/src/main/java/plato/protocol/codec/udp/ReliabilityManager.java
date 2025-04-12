package plato.protocol.codec.udp;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;

/**
 * UDP可靠性管理器接口
 * <p>
 * 管理UDP消息的可靠传输，包括消息重传、确认等
 * </p>
 */
public interface ReliabilityManager {
    
    /**
     * 添加可靠消息
     * <p>
     * 将消息添加到重传队列，直到收到确认或达到最大重传次数
     * </p>
     *
     * @param packet 数据包
     * @param remoteAddress 远程地址
     */
    void addReliableMessage(UdpPacket packet, InetSocketAddress remoteAddress);
    
    /**
     * 确认接收消息
     * <p>
     * 标记指定序列号的消息已被确认，可以从重传队列中移除
     * </p>
     *
     * @param sequenceId 序列号
     * @param remoteAddress 远程地址
     * @return 是否成功确认
     */
    boolean acknowledgeMessage(int sequenceId, InetSocketAddress remoteAddress);
    
    /**
     * 处理接收到的消息
     * <p>
     * 如果是可靠消息，则发送确认；如果是确认消息，则标记对应的消息已确认
     * </p>
     *
     * @param packet 数据包
     * @return 是否是新消息（不是重复接收）
     */
    boolean processReceivedPacket(UdpPacket packet);
    
    /**
     * 创建会话
     * <p>
     * 为新连接创建会话状态
     * </p>
     *
     * @param sessionId 会话ID
     * @param remoteAddress 远程地址
     * @return 会话对象
     */
    UdpSession createSession(long sessionId, InetSocketAddress remoteAddress);
    
    /**
     * 获取会话
     *
     * @param remoteAddress 远程地址
     * @return 会话对象，如果不存在则返回null
     */
    UdpSession getSession(InetSocketAddress remoteAddress);
    
    /**
     * 移除会话
     *
     * @param remoteAddress 远程地址
     * @return 是否成功移除
     */
    boolean removeSession(InetSocketAddress remoteAddress);
    
    /**
     * 设置发送器
     * <p>
     * 用于发送确认消息和重传消息
     * </p>
     *
     * @param sender 发送器函数，接收数据和地址
     */
    void setSender(BiConsumer<byte[], InetSocketAddress> sender);
    
    /**
     * 启动重传定时任务
     */
    void startRetransmissionTask();
    
    /**
     * 停止重传定时任务
     */
    void stopRetransmissionTask();
    
    /**
     * 清理超时会话
     *
     * @param timeoutMillis 超时时间（毫秒）
     * @return 清理的会话数量
     */
    int cleanupTimeoutSessions(long timeoutMillis);
} 