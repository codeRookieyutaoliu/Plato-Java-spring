package plato.common.net;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * 连接配置类
 * <p>
 * 提供连接相关的配置参数，包括超时设置、缓冲区大小等
 * 使用Builder模式方便构建和配置
 * </p>
 */
@Data
@Builder
public class ConnectionConfig {
    
    /**
     * 连接超时时间
     */
    @Builder.Default
    private Duration connectTimeout = Duration.ofSeconds(10);
    
    /**
     * 读取超时时间
     */
    @Builder.Default
    private Duration readTimeout = Duration.ofSeconds(30);
    
    /**
     * 写入超时时间
     */
    @Builder.Default
    private Duration writeTimeout = Duration.ofSeconds(30);
    
    /**
     * 空闲超时时间
     */
    @Builder.Default
    private Duration idleTimeout = Duration.ofMinutes(10);
    
    /**
     * 心跳间隔时间
     */
    @Builder.Default
    private Duration heartbeatInterval = Duration.ofSeconds(30);
    
    /**
     * 发送缓冲区大小(字节)
     */
    @Builder.Default
    private int sendBufferSize = 64 * 1024;
    
    /**
     * 接收缓冲区大小(字节)
     */
    @Builder.Default
    private int receiveBufferSize = 64 * 1024;
    
    /**
     * 最大帧大小(字节)
     */
    @Builder.Default
    private int maxFrameSize = 16 * 1024 * 1024;
    
    /**
     * 是否启用TCP_NODELAY
     */
    @Builder.Default
    private boolean tcpNoDelay = true;
    
    /**
     * 是否启用SO_KEEPALIVE
     */
    @Builder.Default
    private boolean keepAlive = true;
    
    /**
     * 是否启用自动重连
     */
    @Builder.Default
    private boolean autoReconnect = true;
    
    /**
     * 最大重连次数，-1表示无限次
     */
    @Builder.Default
    private int maxReconnectAttempts = 10;
    
    /**
     * 重连间隔时间
     */
    @Builder.Default
    private Duration reconnectInterval = Duration.ofSeconds(5);
    
    /**
     * 默认配置
     * 
     * @return 默认配置对象
     */
    public static ConnectionConfig defaultConfig() {
        return ConnectionConfig.builder().build();
    }
    
    /**
     * 高性能配置
     * 
     * @return 高性能配置对象
     */
    public static ConnectionConfig highPerformanceConfig() {
        return ConnectionConfig.builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(15))
                .writeTimeout(Duration.ofSeconds(15))
                .idleTimeout(Duration.ofMinutes(30))
                .heartbeatInterval(Duration.ofSeconds(60))
                .sendBufferSize(256 * 1024)
                .receiveBufferSize(256 * 1024)
                .maxFrameSize(64 * 1024 * 1024)
                .tcpNoDelay(true)
                .keepAlive(true)
                .build();
    }
    
    /**
     * 低延迟配置
     * 
     * @return 低延迟配置对象
     */
    public static ConnectionConfig lowLatencyConfig() {
        return ConnectionConfig.builder()
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .writeTimeout(Duration.ofSeconds(5))
                .idleTimeout(Duration.ofMinutes(5))
                .heartbeatInterval(Duration.ofSeconds(15))
                .sendBufferSize(32 * 1024)
                .receiveBufferSize(32 * 1024)
                .maxFrameSize(4 * 1024 * 1024)
                .tcpNoDelay(true)
                .keepAlive(true)
                .build();
    }
} 