package plato.gateway.infrastructure.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import plato.common.handler.MessageDispatcher;
import plato.gateway.infrastructure.connection.ConnectionIdGenerator;
import plato.gateway.infrastructure.connection.ConnectionTable;
import plato.gateway.infrastructure.handler.NettyConnectionHandler;
import plato.gateway.infrastructure.handler.NettyMessageDecoder;
import plato.gateway.infrastructure.handler.NettyMessageEncoder;
import plato.gateway.infrastructure.workpool.WorkPool;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty TCP服务器
 * 基于 Netty 的实现
 */
@Slf4j
@Component
@RefreshScope
@Qualifier("nettyTcpServer")
public class NettyServer implements ITcpServer {

    // Boss线程组，用于接收连接
    private EventLoopGroup bossGroup;
    
    // Worker线程组，用于处理IO事件
    private EventLoopGroup workerGroup;
    
    // 服务器通道
    private Channel serverChannel;
    
    // 连接表
    private final ConnectionTable connectionTable;
    
    // 连接ID生成器
    private final ConnectionIdGenerator connectionIdGenerator;
    
    // 工作池
    private final WorkPool workPool;
    
    // 消息分发器
    private final MessageDispatcher messageDispatcher;
    
    // 运行标志
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 服务器配置
    @Value("${tcp.server.port:8888}")
    private int port;
    
    @Value("${tcp.server.boss-thread-count:1}")
    private int bossThreadCount;
    
    @Value("${tcp.server.worker-thread-count:4}")
    private int workerThreadCount;
    
    @Value("${tcp.server.use-epoll:false}")
    private boolean useEpoll;
    
    @Value("${tcp.server.idle-timeout-seconds:300}")
    private int idleTimeoutSeconds;
    
    @Value("${tcp.server.max-frame-length:65536}")
    private int maxFrameLength;

    /**
     * 构造函数
     *
     * @param connectionTable 连接表
     * @param connectionIdGenerator 连接ID生成器
     * @param workPool 工作池
     * @param messageDispatcher 消息分发器
     */
    @Autowired
    public NettyServer(ConnectionTable connectionTable, ConnectionIdGenerator connectionIdGenerator, 
                      WorkPool workPool, MessageDispatcher messageDispatcher) {
        this.connectionTable = connectionTable;
        this.connectionIdGenerator = connectionIdGenerator;
        this.workPool = workPool;
        this.messageDispatcher = messageDispatcher;
    }

    /**
     * 初始化服务器
     */
    @PostConstruct
    @Override
    public void init() {
        log.info("初始化 Netty TCP服务器: port={}, bossThreadCount={}, workerThreadCount={}, useEpoll={}, idleTimeoutSeconds={}, maxFrameLength={}",
                port, bossThreadCount, workerThreadCount, useEpoll, idleTimeoutSeconds, maxFrameLength);
    }

    /**
     * 启动服务器
     */
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            log.info("启动 Netty TCP服务器");
            
            try {
                // 创建线程组
                if (useEpoll) {
                    bossGroup = new EpollEventLoopGroup(bossThreadCount);
                    workerGroup = new EpollEventLoopGroup(workerThreadCount);
                } else {
                    bossGroup = new NioEventLoopGroup(bossThreadCount);
                    workerGroup = new NioEventLoopGroup(workerThreadCount);
                }
                
                // 创建服务器引导
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                        .channel(useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();
                                
                                // 空闲连接检测
                                pipeline.addLast(new IdleStateHandler(idleTimeoutSeconds, 0, 0, TimeUnit.SECONDS));
                                
                                // 消息编解码
                                pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength, 0, 4, 0, 4));
                                pipeline.addLast(new LengthFieldPrepender(4));
                                pipeline.addLast(new NettyMessageDecoder(messageDispatcher));
                                pipeline.addLast(new NettyMessageEncoder());
                                
                                // 连接处理器
                                pipeline.addLast(new NettyConnectionHandler(connectionTable, connectionIdGenerator, workPool));
                            }
                        });
                
                // 绑定端口并启动服务器
                ChannelFuture future = bootstrap.bind(port).sync();
                serverChannel = future.channel();
                
                log.info("Netty TCP服务器启动完成，监听端口: {}", port);
                
                // 启动连接清理任务
                startCleanTask();
            } catch (Exception e) {
                log.error("启动 Netty TCP服务器失败", e);
                stop();
            }
        }
    }

    /**
     * 启动连接清理任务
     */
    private void startCleanTask() {
        workPool.submit(() -> {
            log.info("启动连接清理任务: interval={}s", 60);
            
            while (running.get()) {
                try {
                    // 休眠一段时间
                    Thread.sleep(60 * 1000L);
                    
                    // 清理空闲连接由 IdleStateHandler 和 NettyConnectionHandler 处理
                    log.debug("当前连接数: {}", connectionTable.size());
                } catch (InterruptedException e) {
                    log.error("连接清理任务被中断", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("连接清理任务异常", e);
                }
            }
            
            log.info("连接清理任务已停止");
        });
    }

    /**
     * 关闭连接
     *
     * @param connectionId 连接ID
     * @return 是否成功
     */
    @Override
    public boolean closeConnection(long connectionId) {
        return connectionTable.closeConnection(connectionId);
    }

    /**
     * 发送消息
     *
     * @param connectionId 连接ID
     * @param payload 消息内容
     * @return 是否成功
     */
    @Override
    public boolean sendMessage(long connectionId, byte[] payload) {
        return connectionTable.sendMessage(connectionId, payload);
    }

    /**
     * 停止服务器
     */
    @PreDestroy
    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("停止 Netty TCP服务器");
            
            try {
                // 关闭服务器通道
                if (serverChannel != null) {
                    serverChannel.close().sync();
                }
                
                // 关闭线程组
                if (bossGroup != null) {
                    bossGroup.shutdownGracefully();
                }
                if (workerGroup != null) {
                    workerGroup.shutdownGracefully();
                }
                
                log.info("Netty TCP服务器已停止");
            } catch (Exception e) {
                log.error("停止 Netty TCP服务器失败", e);
            }
        }
    }

    /**
     * 获取连接数量
     *
     * @return 连接数量
     */
    @Override
    public int getConnectionCount() {
        return connectionTable.size();
    }
} 