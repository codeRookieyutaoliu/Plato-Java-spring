package plato.network.infrastructure.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.application.service.MessageProcessingService;
import plato.network.application.service.ConnectionMonitorService;
import plato.network.domain.service.ConnectionManager;
import plato.network.domain.valueobject.ConnectionStatistics;
import plato.network.infrastructure.netty.codec.MessageDecoder;
import plato.network.infrastructure.netty.codec.MessageEncoder;
import plato.network.infrastructure.netty.handler.MessageChannelHandler;

import java.util.concurrent.TimeUnit;

/**
 * Netty服务器
 * <p>
 * 基于Netty实现的TCP服务器
 * </p>
 * 
 * 对应Go项目中的TCP服务器实现
 */
public class NettyServer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);
    
    /**
     * 默认主线程组大小
     */
    private static final int DEFAULT_BOSS_THREADS = 1;
    
    /**
     * 默认工作线程组大小
     */
    private static final int DEFAULT_WORKER_THREADS = 0; // 0表示使用Netty默认值（CPU核心数 * 2）
    
    /**
     * 默认连接空闲超时时间（秒）
     */
    private static final int DEFAULT_IDLE_TIMEOUT = 180;
    
    /**
     * 服务器地址
     */
    private final String host;
    
    /**
     * 服务器端口
     */
    private final int port;
    
    /**
     * 主线程组大小
     */
    private final int bossThreads;
    
    /**
     * 工作线程组大小
     */
    private final int workerThreads;
    
    /**
     * 连接空闲超时时间（秒）
     */
    private final int idleTimeout;
    
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
     * 主线程组
     */
    private EventLoopGroup bossGroup;
    
    /**
     * 工作线程组
     */
    private EventLoopGroup workerGroup;
    
    /**
     * 服务器通道
     */
    private Channel serverChannel;
    
    /**
     * 服务器是否运行
     */
    private volatile boolean running;
    
    /**
     * 构造函数
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     */
    public NettyServer(String host, int port, ConnectionManager connectionManager, MessageProcessingService messageProcessingService) {
        this(host, port, DEFAULT_BOSS_THREADS, DEFAULT_WORKER_THREADS, DEFAULT_IDLE_TIMEOUT, connectionManager, messageProcessingService);
    }
    
    /**
     * 构造函数
     *
     * @param host 服务器地址
     * @param port 服务器端口
     * @param bossThreads 主线程组大小
     * @param workerThreads 工作线程组大小
     * @param idleTimeout 连接空闲超时时间（秒）
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     */
    public NettyServer(String host, int port, int bossThreads, int workerThreads, int idleTimeout, 
                        ConnectionManager connectionManager, MessageProcessingService messageProcessingService) {
        this.host = host;
        this.port = port;
        this.bossThreads = bossThreads;
        this.workerThreads = workerThreads;
        this.idleTimeout = idleTimeout;
        this.connectionManager = connectionManager;
        this.messageProcessingService = messageProcessingService;
        
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
     * 启动服务器
     *
     * @throws Exception 如果启动失败
     */
    public void start() throws Exception {
        if (running) {
            LOGGER.warn("服务器已经在运行，地址={}:{}", host, port);
            return;
        }
        
        LOGGER.info("正在启动Netty服务器，地址={}:{}", host, port);
        
        // 创建线程组
        bossGroup = new NioEventLoopGroup(bossThreads);
        workerGroup = new NioEventLoopGroup(workerThreads);
        
        try {
            // 创建消息处理器
            MessageChannelHandler messageChannelHandler = new MessageChannelHandler(
                    connectionManager, messageProcessingService, statistics);
            
            // 创建服务器引导
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
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
            
            // 绑定端口并启动服务器
            ChannelFuture future = bootstrap.bind(host, port).sync();
            serverChannel = future.channel();
            running = true;
            
            LOGGER.info("Netty服务器启动成功，地址={}:{}", host, port);
            
            // 启动连接监控服务
            monitorService.start();
            LOGGER.info("连接监控服务启动成功");
            
            // 等待服务器关闭
            serverChannel.closeFuture().addListener(f -> {
                LOGGER.info("Netty服务器已关闭，地址={}:{}", host, port);
                running = false;
            });
        } catch (Exception e) {
            LOGGER.error("启动Netty服务器失败，地址={}:{}", host, port, e);
            stop();
            throw e;
        }
    }
    
    /**
     * 停止服务器
     */
    public void stop() {
        if (!running) {
            LOGGER.warn("服务器未运行，无需停止");
            return;
        }
        
        LOGGER.info("正在停止Netty服务器，地址={}:{}", host, port);
        
        try {
            // 停止连接监控服务
            monitorService.stop();
            LOGGER.info("连接监控服务已停止");
            
            // 关闭服务器通道
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
        } catch (Exception e) {
            LOGGER.error("关闭服务器通道失败", e);
        } finally {
            // 关闭线程组
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            
            running = false;
            LOGGER.info("Netty服务器已停止，地址={}:{}", host, port);
        }
    }
    
    /**
     * 服务器是否运行
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
     * 获取连接统计信息
     *
     * @return 连接统计信息
     */
    public ConnectionStatistics getStatistics() {
        return statistics;
    }
} 