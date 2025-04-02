package plato.gateway.infrastructure.connection;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接领域模型
 * 表示一个基于 Java NIO 的 TCP 连接，包含连接的基本信息和状态
 * 对应Go版本的connection结构体
 */
@Slf4j
@Getter
@Setter
public class NIOConnection implements IConnection {
    
    // 连接ID，对应Go版本的id
    private long id;
    
    // 远程地址，对应Go版本的remoteAddr
    private String remoteAddress;
    
    // 创建时间，对应Go版本的createTime
    private final Instant createTime;
    
    // 最后活动时间，对应Go版本的lastActiveTime
    private Instant lastActiveTime;
    
    // 是否活跃，对应Go版本的active
    private boolean active;
    
    // 套接字通道，对应Go版本的conn
    private SocketChannel socketChannel;
    
    // 事件轮询器ID，对应Go版本的epollerId
    private int epollerId;
    
    // 接收的字节数，对应Go版本的recvBytes
    private final AtomicLong receivedBytes = new AtomicLong(0);
    
    // 发送的字节数，对应Go版本的sendBytes
    private final AtomicLong sentBytes = new AtomicLong(0);
    
    /**
     * 默认构造函数
     */
    public NIOConnection() {
        this.createTime = Instant.now();
        this.lastActiveTime = this.createTime;
        this.active = true;
    }
    
    /**
     * 构造函数
     *
     * @param id            连接ID
     * @param socketChannel 套接字通道
     * @param remoteAddress 远程地址
     */
    public NIOConnection(long id, SocketChannel socketChannel, String remoteAddress) {
        this.id = id;
        this.socketChannel = socketChannel;
        this.remoteAddress = remoteAddress;
        this.createTime = Instant.now();
        this.lastActiveTime = this.createTime;
        this.active = true;
    }
    
    /**
     * 更新最后活动时间
     * 对应Go版本的updateLastActiveTime方法
     */
    @Override
    public void updateLastActiveTime() {
        this.lastActiveTime = Instant.now();
    }
    
    /**
     * 检查是否空闲超时
     * 对应Go版本的isIdleTimeout方法
     *
     * @param maxIdleTimeSeconds 最大空闲时间（秒）
     * @return 是否空闲超时
     */
    @Override
    public boolean isIdleTimeout(int maxIdleTimeSeconds) {
        return Instant.now().getEpochSecond() - lastActiveTime.getEpochSecond() > maxIdleTimeSeconds;
    }
    
    /**
     * 关闭连接
     * 对应Go版本的Close方法
     */
    @Override
    public void close() {
        if (active) {
            try {
                socketChannel.close();
                active = false;
                log.info("连接已关闭: id={}, remoteAddress={}", id, remoteAddress);
            } catch (IOException e) {
                log.error("关闭连接失败: id={}", id, e);
            }
        }
    }
    
    /**
     * 发送消息
     * 对应Go版本的Send方法
     *
     * @param payload 消息内容
     * @return 是否发送成功
     */
    @Override
    public boolean sendMessage(byte[] payload) {
        if (!active) {
            log.warn("连接不活跃: id={}", id);
            return false;
        }
        
        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            int bytesWritten = socketChannel.write(buffer);
            sentBytes.addAndGet(bytesWritten);
            updateLastActiveTime();
            return true;
        } catch (IOException e) {
            log.error("发送消息失败: id={}", id, e);
            return false;
        }
    }
    
    /**
     * 接收消息回调
     * 对应Go版本的onReceive方法
     *
     * @param data 接收到的数据
     */
    @Override
    public void onReceive(byte[] data) {
        receivedBytes.addAndGet(data.length);
        updateLastActiveTime();
        log.debug("接收到消息: id={}, length={}", id, data.length);
        
        // 这里可以添加消息处理逻辑
        // 例如：解析消息、处理业务逻辑等
    }
    
    /**
     * 获取连接地址信息
     * 对应Go版本的RemoteAddr方法
     *
     * @return 连接地址信息
     */
    public String getAddressInfo() {
        return remoteAddress;
    }
    
    /**
     * 获取接收的字节数
     * 对应Go版本的GetRecvBytes方法
     *
     * @return 接收的字节数
     */
    @Override
    public long getReceivedBytes() {
        return receivedBytes.get();
    }
    
    /**
     * 获取发送的字节数
     * 对应Go版本的GetSendBytes方法
     *
     * @return 发送的字节数
     */
    @Override
    public long getSentBytes() {
        return sentBytes.get();
    }
} 