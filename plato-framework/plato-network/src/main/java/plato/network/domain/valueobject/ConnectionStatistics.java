package plato.network.domain.valueobject;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接统计值对象
 * <p>
 * 用于收集和统计网络连接相关的指标
 * </p>
 * 
 * 对应Go项目中的统计功能
 */
public class ConnectionStatistics {
    
    /**
     * 创建的连接总数
     */
    private final AtomicLong totalConnections = new AtomicLong(0);
    
    /**
     * 当前活跃连接数
     */
    private final AtomicLong activeConnections = new AtomicLong(0);
    
    /**
     * 已认证的连接数
     */
    private final AtomicLong authenticatedConnections = new AtomicLong(0);
    
    /**
     * 关闭的连接数
     */
    private final AtomicLong closedConnections = new AtomicLong(0);
    
    /**
     * 接收的消息总数
     */
    private final AtomicLong receivedMessages = new AtomicLong(0);
    
    /**
     * 发送的消息总数
     */
    private final AtomicLong sentMessages = new AtomicLong(0);
    
    /**
     * 接收的总字节数
     */
    private final AtomicLong receivedBytes = new AtomicLong(0);
    
    /**
     * 发送的总字节数
     */
    private final AtomicLong sentBytes = new AtomicLong(0);
    
    /**
     * 处理失败的消息数
     */
    private final AtomicLong failedMessages = new AtomicLong(0);
    
    /**
     * 增加创建的连接总数
     */
    public void incrementTotalConnections() {
        totalConnections.incrementAndGet();
    }
    
    /**
     * 增加活跃连接数
     */
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }
    
    /**
     * 减少活跃连接数
     */
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }
    
    /**
     * 增加已认证的连接数
     */
    public void incrementAuthenticatedConnections() {
        authenticatedConnections.incrementAndGet();
    }
    
    /**
     * 减少已认证的连接数
     */
    public void decrementAuthenticatedConnections() {
        authenticatedConnections.decrementAndGet();
    }
    
    /**
     * 增加关闭的连接数
     */
    public void incrementClosedConnections() {
        closedConnections.incrementAndGet();
    }
    
    /**
     * 增加接收的消息数
     */
    public void incrementReceivedMessages() {
        receivedMessages.incrementAndGet();
    }
    
    /**
     * 增加发送的消息数
     */
    public void incrementSentMessages() {
        sentMessages.incrementAndGet();
    }
    
    /**
     * 增加接收的字节数
     *
     * @param bytes 接收的字节数
     */
    public void addReceivedBytes(long bytes) {
        receivedBytes.addAndGet(bytes);
    }
    
    /**
     * 增加发送的字节数
     *
     * @param bytes 发送的字节数
     */
    public void addSentBytes(long bytes) {
        sentBytes.addAndGet(bytes);
    }
    
    /**
     * 增加处理失败的消息数
     */
    public void incrementFailedMessages() {
        failedMessages.incrementAndGet();
    }
    
    /**
     * 获取创建的连接总数
     *
     * @return 创建的连接总数
     */
    public long getTotalConnections() {
        return totalConnections.get();
    }
    
    /**
     * 获取当前活跃连接数
     *
     * @return 当前活跃连接数
     */
    public long getActiveConnections() {
        return activeConnections.get();
    }
    
    /**
     * 获取已认证的连接数
     *
     * @return 已认证的连接数
     */
    public long getAuthenticatedConnections() {
        return authenticatedConnections.get();
    }
    
    /**
     * 获取关闭的连接数
     *
     * @return 关闭的连接数
     */
    public long getClosedConnections() {
        return closedConnections.get();
    }
    
    /**
     * 获取接收的消息总数
     *
     * @return 接收的消息总数
     */
    public long getReceivedMessages() {
        return receivedMessages.get();
    }
    
    /**
     * 获取发送的消息总数
     *
     * @return 发送的消息总数
     */
    public long getSentMessages() {
        return sentMessages.get();
    }
    
    /**
     * 获取接收的总字节数
     *
     * @return 接收的总字节数
     */
    public long getReceivedBytes() {
        return receivedBytes.get();
    }
    
    /**
     * 获取发送的总字节数
     *
     * @return 发送的总字节数
     */
    public long getSentBytes() {
        return sentBytes.get();
    }
    
    /**
     * 获取处理失败的消息数
     *
     * @return 处理失败的消息数
     */
    public long getFailedMessages() {
        return failedMessages.get();
    }
    
    @Override
    public String toString() {
        return "ConnectionStatistics{" +
                "totalConnections=" + getTotalConnections() +
                ", activeConnections=" + getActiveConnections() +
                ", authenticatedConnections=" + getAuthenticatedConnections() +
                ", closedConnections=" + getClosedConnections() +
                ", receivedMessages=" + getReceivedMessages() +
                ", sentMessages=" + getSentMessages() +
                ", receivedBytes=" + getReceivedBytes() +
                ", sentBytes=" + getSentBytes() +
                ", failedMessages=" + getFailedMessages() +
                '}';
    }
} 