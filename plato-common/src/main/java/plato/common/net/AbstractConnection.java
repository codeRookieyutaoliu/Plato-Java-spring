package plato.common.net;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 抽象连接基类
 * <p>
 * 实现Connection接口的通用功能，为具体连接实现提供基础
 * 子类只需实现特定的网络传输逻辑
 * </p>
 */
@Slf4j
public abstract class AbstractConnection implements Connection {
    
    /**
     * 连接ID
     */
    @Getter
    protected final String id;
    
    /**
     * 远程地址
     */
    @Getter
    protected final String remoteAddress;
    
    /**
     * 创建时间
     */
    @Getter
    protected final Instant createTime;
    
    /**
     * 最后活动时间
     */
    @Getter
    protected Instant lastActiveTime;
    
    /**
     * 连接事件处理器
     */
    protected ConnectionEventHandler eventHandler;
    
    /**
     * 连接状态（是否已关闭）
     */
    protected final AtomicBoolean closed = new AtomicBoolean(false);
    
    /**
     * 接收的字节数
     */
    protected final AtomicLong receivedBytes = new AtomicLong(0);
    
    /**
     * 发送的字节数
     */
    protected final AtomicLong sentBytes = new AtomicLong(0);
    
    /**
     * 连接配置
     */
    protected final ConnectionConfig config;
    
    /**
     * 构造函数
     *
     * @param id 连接ID
     * @param remoteAddress 远程地址
     * @param config 连接配置
     */
    protected AbstractConnection(String id, String remoteAddress, ConnectionConfig config) {
        this.id = id;
        this.remoteAddress = remoteAddress;
        this.createTime = Instant.now();
        this.lastActiveTime = Instant.now();
        this.config = config != null ? config : ConnectionConfig.defaultConfig();
    }
    
    /**
     * 更新最后活动时间
     */
    @Override
    public void updateLastActiveTime() {
        this.lastActiveTime = Instant.now();
    }
    
    /**
     * 设置连接事件处理器
     *
     * @param handler 事件处理器
     */
    @Override
    public void setEventHandler(ConnectionEventHandler handler) {
        this.eventHandler = handler;
    }
    
    /**
     * 检查连接是否活跃
     *
     * @return 如果连接未关闭，返回true
     */
    @Override
    public boolean isActive() {
        return !closed.get();
    }
    
    /**
     * 异步发送数据的默认实现
     * 子类可以覆盖此方法提供更高效的实现
     *
     * @param data 要发送的数据
     * @return 发送结果的Future
     */
    @Override
    public CompletableFuture<Boolean> sendAsync(ByteBuffer data) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            boolean result = send(data);
            future.complete(result);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }
    
    /**
     * 触发接收到数据事件
     *
     * @param data 接收到的数据
     */
    protected void fireDataReceived(ByteBuffer data) {
        if (eventHandler != null && data != null) {
            updateLastActiveTime();
            receivedBytes.addAndGet(data.remaining());
            try {
                eventHandler.onReceive(this, data);
            } catch (Exception e) {
                log.error("处理接收数据时发生异常: connectionId={}", id, e);
                fireError(e);
            }
        }
    }
    
    /**
     * 触发连接关闭事件
     */
    protected void fireClosed() {
        if (eventHandler != null && closed.compareAndSet(false, true)) {
            try {
                eventHandler.onClose(this);
            } catch (Exception e) {
                log.error("处理连接关闭事件时发生异常: connectionId={}", id, e);
            }
        }
    }
    
    /**
     * 触发连接错误事件
     *
     * @param error 错误信息
     */
    protected void fireError(Throwable error) {
        if (eventHandler != null) {
            try {
                eventHandler.onError(this, error);
            } catch (Exception e) {
                log.error("处理连接错误事件时发生异常: connectionId={}", id, e);
            }
        }
    }
    
    /**
     * 获取接收的字节数
     *
     * @return 接收的字节总数
     */
    public long getReceivedBytes() {
        return receivedBytes.get();
    }
    
    /**
     * 获取发送的字节数
     *
     * @return 发送的字节总数
     */
    public long getSentBytes() {
        return sentBytes.get();
    }
} 