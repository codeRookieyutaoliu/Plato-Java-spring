package plato.gateway.infrastructure.connection;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import plato.gateway.domain.model.Message;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Netty 连接
 * 表示一个基于 Netty 的连接
 */
@Slf4j
@Getter
public class NettyConnection implements IConnection {

    // 连接ID属性键
    public static final AttributeKey<Long> CONNECTION_ID_KEY = AttributeKey.valueOf("connectionId");
    
    // 连接ID
    private final long id;
    
    // Netty 通道
    private final Channel channel;
    
    // 远程地址
    private final String remoteAddress;
    
    // 创建时间
    private final Instant createTime;
    
    // 最后活动时间
    private Instant lastActiveTime;
    
    // 接收的字节数
    private final AtomicLong receivedBytes = new AtomicLong(0);
    
    // 发送的字节数
    private final AtomicLong sentBytes = new AtomicLong(0);

    /**
     * 构造函数
     *
     * @param id 连接ID
     * @param channel Netty 通道
     * @param remoteAddress 远程地址
     */
    public NettyConnection(long id, Channel channel, String remoteAddress) {
        this.id = id;
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.createTime = Instant.now();
        this.lastActiveTime = Instant.now();
    }

    /**
     * 发送消息
     *
     * @param payload 消息内容
     * @return 是否成功
     */
    @Override
    public boolean sendMessage(byte[] payload) {
        try {
            // 创建消息对象
            Message message = Message.builder().payload(payload).build();

            
            // 发送消息
            channel.writeAndFlush(message);
            
            // 更新统计信息
            sentBytes.addAndGet(payload.length);
            
            // 更新最后活动时间
            updateLastActiveTime();
            
            return true;
        } catch (Exception e) {
            log.error("发送消息失败: id={}, cause={}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() {
        try {
            channel.close();
        } catch (Exception e) {
            log.error("关闭连接失败: id={}, cause={}", id, e.getMessage());
        }
    }

    /**
     * 更新最后活动时间
     */
    @Override
    public void updateLastActiveTime() {
        this.lastActiveTime = Instant.now();
    }

    /**
     * 检查连接是否空闲超时
     *
     * @param maxIdleTimeSeconds 最大空闲时间（秒）
     * @return 是否空闲超时
     */
    @Override
    public boolean isIdleTimeout(int maxIdleTimeSeconds) {
        return Instant.now().minusSeconds(maxIdleTimeSeconds).isAfter(lastActiveTime);
    }
    
    /**
     * 接收消息回调
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
} 
 