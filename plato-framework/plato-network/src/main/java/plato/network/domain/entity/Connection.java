package plato.network.domain.entity;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 网络连接实体
 * <p>
 * 表示一个网络连接，包含连接ID、远程地址、本地地址、创建时间等信息
 * </p>
 * 
 * 对应Go项目中的Connection结构体
 */
public class Connection {
    
    /**
     * 连接ID生成器
     */
    private static final AtomicLong CONNECTION_ID_GENERATOR = new AtomicLong(1);
    
    /**
     * 连接ID
     */
    private final long id;
    
    /**
     * 远程地址
     */
    private final InetSocketAddress remoteAddress;
    
    /**
     * 本地地址
     */
    private final InetSocketAddress localAddress;
    
    /**
     * 连接创建时间
     */
    private final long createTime;
    
    /**
     * 最后活动时间
     */
    private volatile long lastActiveTime;
    
    /**
     * 是否已认证
     */
    private volatile boolean authenticated;
    
    /**
     * 用户ID
     */
    private volatile String userId;
    
    /**
     * 设备ID
     */
    private volatile String deviceId;
    
    /**
     * 会话ID
     */
    private volatile long sessionId;
    
    /**
     * 客户端ID
     */
    private volatile long clientId;
    
    /**
     * 最大客户端消息ID
     */
    private volatile long maxClientMsgId;
    
    /**
     * 构造函数
     *
     * @param remoteAddress 远程地址
     * @param localAddress 本地地址
     */
    public Connection(InetSocketAddress remoteAddress, InetSocketAddress localAddress) {
        this.id = generateConnectionId();
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.createTime = Instant.now().toEpochMilli();
        this.lastActiveTime = this.createTime;
        this.authenticated = false;
    }
    
    /**
     * 生成连接ID
     *
     * @return 连接ID
     */
    private static long generateConnectionId() {
        return CONNECTION_ID_GENERATOR.getAndIncrement();
    }
    
    /**
     * 更新最后活动时间
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = Instant.now().toEpochMilli();
    }
    
    /**
     * 设置认证信息
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param sessionId 会话ID
     * @param clientId 客户端ID
     */
    public void authenticate(String userId, String deviceId, long sessionId, long clientId) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.sessionId = sessionId;
        this.clientId = clientId;
        this.authenticated = true;
        updateLastActiveTime();
    }
    
    /**
     * 更新最大客户端消息ID
     *
     * @param msgId 客户端消息ID
     * @return 是否更新成功
     */
    public boolean updateMaxClientMsgId(long msgId) {
        if (msgId > this.maxClientMsgId) {
            this.maxClientMsgId = msgId;
            return true;
        }
        return false;
    }
    
    /**
     * 获取连接ID
     *
     * @return 连接ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    
    /**
     * 获取本地地址
     *
     * @return 本地地址
     */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }
    
    /**
     * 获取连接创建时间
     *
     * @return 连接创建时间
     */
    public long getCreateTime() {
        return createTime;
    }
    
    /**
     * 获取最后活动时间
     *
     * @return 最后活动时间
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }
    
    /**
     * 是否已认证
     *
     * @return 是否已认证
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    public String getDeviceId() {
        return deviceId;
    }
    
    /**
     * 获取会话ID
     *
     * @return 会话ID
     */
    public long getSessionId() {
        return sessionId;
    }
    
    /**
     * 获取客户端ID
     *
     * @return 客户端ID
     */
    public long getClientId() {
        return clientId;
    }
    
    /**
     * 获取最大客户端消息ID
     *
     * @return 最大客户端消息ID
     */
    public long getMaxClientMsgId() {
        return maxClientMsgId;
    }
    
    @Override
    public String toString() {
        return "Connection{" +
                "id=" + id +
                ", remoteAddress=" + remoteAddress +
                ", localAddress=" + localAddress +
                ", createTime=" + createTime +
                ", lastActiveTime=" + lastActiveTime +
                ", authenticated=" + authenticated +
                ", userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", sessionId=" + sessionId +
                ", clientId=" + clientId +
                ", maxClientMsgId=" + maxClientMsgId +
                '}';
    }
} 