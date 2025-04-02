package plato.network.infrastructure.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.application.service.MessageProcessingService;
import plato.network.application.service.ConnectionMonitorService;
import plato.network.domain.entity.Connection;
import plato.network.domain.service.ConnectionManager;
import plato.network.domain.valueobject.ConnectionStatistics;
import plato.network.infrastructure.netty.codec.MessageDecoder;
import plato.network.infrastructure.netty.codec.MessageEncoder;
import plato.network.infrastructure.netty.handler.MessageChannelHandler;

import java.util.concurrent.TimeUnit;

/**
 * Netty客户端
 * <p>
 * 基于Netty实现的TCP客户端
 * </p>
 * 
 * 对应Go项目中的TCP客户端实现
 */
public class NettyClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);
    
    /**
     * 默认工作线程组大小
     */
    private static final int DEFAULT_WORKER_THREADS = 1;
    
    /**
     * 默认连接空闲超时时间（秒）
     */
    private static final int DEFAULT_IDLE_TIMEOUT = 180;
    
    /**
     * 默认连接超时时间（毫秒）
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    
    /**
     * 默认重连间隔（毫秒）
     */
    private static final int DEFAULT_RECONNECT_INTERVAL = 3000;
    
    /**
     * 服务器地址
     */
    private final String host;
    
    /**
     * 服务器端口
     */
    private final int port;
    
    /**
     * 工作线程组大小
     */
    private final int workerThreads;
    
    /**
     * 连接空闲超时时间（秒）
     */
    private final int idleTimeout;
    
    /**
     * 连接超时时间（毫秒）
     */
    private final int connectTimeout;
    
    /**
     * 重连间隔（毫秒）
     */
    private final int reconnectInterval;
    
    /**
     * 连接管理器
     */
    private final ConnectionManager connectionManager;
    
    /**
     * 消息处理服务
     */
    private final MessageProcessingService messageProcessingService;
    
    /**
     * 连接统计信息
     */
    private final ConnectionStatistics statistics;
    
    /**
     * 连接监控服务
     */
    private final ConnectionMonitorService monitorService;
    
    /**
     * 工作线程组
     */
    private EventLoopGroup workerGroup;
    
    /**
     * 客户端引导
     */
    private Bootstrap bootstrap;
    
    /**
     * 客户端通道
     */
    private Channel clientChannel;
    
    /**
     * 客户端连接
     */
    private Connection connection;
    
    /**
     * 客户端是否运行
     */
    private volatile boolean running;
    
    /**
     * 是否自动重连
     */
    private volatile boolean autoReconnect;
    
    /**
     * 构造函数
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     */
    public NettyClient(String host, int port, ConnectionManager connectionManager, MessageProcessingService messageProcessingService) {
        this(host, port, DEFAULT_WORKER_THREADS, DEFAULT_IDLE_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_RECONNECT_INTERVAL, connectionManager, messageProcessingService);
    }
    
    /**
     * 构造函数
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @param workerThreads 工作线程组大小
     * @param idleTimeout 连接空闲超时时间（秒）
     * @param connectTimeout 连接超时时间（毫秒）
     * @param reconnectInterval 重连间隔（毫秒）
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     */
    public NettyClient(String host, int port, int workerThreads, int idleTimeout, int connectTimeout, int reconnectInterval,
                       ConnectionManager connectionManager, MessageProcessingService messageProcessingService) {
        this.host = host;
        this.port = port;
        this.workerThreads = workerThreads;
        this.idleTimeout = idleTimeout;
        this.connectTimeout = connectTimeout;
        this.reconnectInterval = reconnectInterval;
        this.connectionManager = connectionManager;
        this.messageProcessingService = messageProcessingService;
        this.autoReconnect = true;

        // 使用默认的连接统计信息
        if (connectionManager instanceof DefaultConnectionManager) {
            this.statistics = ((DefaultConnectionManager) connectionManager).getStatistics();
        } else {
            this.statistics = new ConnectionStatistics();
        }
        
        // 创建连接监控服务
        this.monitorService = new ConnectionMonitorService(connectionManager, statistics, idleTimeout * 1000, 60, 60);
    }
    
    /**
     * 初始化客户端
     */
    private void init() {
        // 创建工作线程组
        workerGroup = new NioEventLoopGroup(workerThreads);
        
        // 创建消息处理器
        MessageChannelHandler messageChannelHandler = new MessageChannelHandler(
                connectionManager, messageProcessingService, statistics);
        
        // 创建客户端引导
        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        
                        // 添加空闲检测处理器
                        pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, idleTimeout, TimeUnit.SECONDS));
                        
                        // 添加消息编解码器
                        pipeline.addLast("decoder", new MessageDecoder());
                        pipeline.addLast("encoder", new MessageEncoder());
                        
                        // 添加消息处理器
                        pipeline.addLast("messageHandler", messageChannelHandler);
                    }
                });
    }
    
    /**
     * 连接服务器
     *
     * @return 连接成功返回true，否则返回false
     */
    public boolean connect() {
        if (running) {
            LOGGER.warn("客户端已经在运行，地址={}:{}", host, port);
            return true;
        }
        
        LOGGER.info("正在连接服务器，地址={}:{}", host, port);
        
        // 初始化客户端
        if (bootstrap == null) {
            init();
        }
        
        try {
            // 连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (!future.isSuccess()) {
                LOGGER.error("连接服务器失败，地址={}:{}", host, port, future.cause());
                return false;
            }
            
            clientChannel = future.channel();
            running = true;
            
            // 注册连接
            connection = connectionManager.createConnection(clientChannel);
            
            LOGGER.info("连接服务器成功，地址={}:{}, 连接ID={}", host, port, connection.getId());
            
            // 启动连接监控服务
            monitorService.start();
            LOGGER.info("连接监控服务启动成功");
            
            // 设置连接关闭监听器
            clientChannel.closeFuture().addListener(f -> {
                LOGGER.info("与服务器的连接已关闭，地址={}:{}", host, port);
                running = false;
                
                // 自动重连
                if (autoReconnect) {
                    scheduleReconnect();
                }
            });
            
            return true;
        } catch (Exception e) {
            LOGGER.error("连接服务器失败，地址={}:{}", host, port, e);
            running = false;
            
            // 自动重连
            if (autoReconnect) {
                scheduleReconnect();
            }
            
            return false;
        }
    }
    
    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        LOGGER.info("计划在{}毫秒后重新连接服务器，地址={}:{}", reconnectInterval, host, port);
        
        // 安排重连任务
        workerGroup.schedule(() -> {
            LOGGER.info("正在尝试重新连接服务器，地址={}:{}", host, port);
            connect();
        }, reconnectInterval, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        if (!running) {
            LOGGER.warn("客户端未运行，无需断开连接");
            return;
        }
        
        LOGGER.info("正在断开与服务器的连接，地址={}:{}", host, port);
        
        try {
            // 停止连接监控服务
            monitorService.stop();
            LOGGER.info("连接监控服务已停止");
            
            // 禁用自动重连
            autoReconnect = false;
            
            // 断开连接
            if (clientChannel != null) {
                clientChannel.close().sync();
            }
        } catch (Exception e) {
            LOGGER.error("断开连接失败", e);
        } finally {
            // 关闭工作线程组
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
                workerGroup = null;
            }
            
            // 清空引导和通道
            bootstrap = null;
            clientChannel = null;
            connection = null;
            
            running = false;
            LOGGER.info("已断开与服务器的连接，地址={}:{}", host, port);
        }
    }
    
    /**
     * 发送消息
     *
     * @param message 消息对象
     * @return 发送成功返回true，否则返回false
     */
    public boolean sendMessage(Object message) {
        if (!running || clientChannel == null || !clientChannel.isActive()) {
            LOGGER.error("客户端未运行或通道不活跃，无法发送消息");
            return false;
        }
        
        try {
            // 发送消息
            clientChannel.writeAndFlush(message);
            
            // 更新统计信息
            statistics.incrementSentMessages();
            
            return true;
        } catch (Exception e) {
            LOGGER.error("发送消息失败", e);
            
            // 更新统计信息
            statistics.incrementFailedMessages();
            
            return false;
        }
    }
    
    /**
     * 客户端是否运行
     *
     * @return 是否运行
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 获取服务器地址
     *
     * @return 服务器地址
     */
    public String getHost() {
        return host;
    }
    
    /**
     * 获取服务器端口
     *
     * @return 服务器端口
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 获取客户端连接
     *
     * @return 客户端连接
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * 设置自动重连
     *
     * @param autoReconnect 是否自动重连
     */
    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }
    
    /**
     * 是否自动重连
     *
     * @return 是否自动重连
     */
    public boolean isAutoReconnect() {
        return autoReconnect;
    }
    
    /**
     * 获取连接统计信息
     *
     * @return 连接统计信息
     */
    public ConnectionStatistics getStatistics() {
        return statistics;
    }
} 