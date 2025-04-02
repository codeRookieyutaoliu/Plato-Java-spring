package plato.gateway.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import plato.gateway.infrastructure.connection.NIOConnection;
import plato.gateway.infrastructure.connection.ConnectionTable;
import plato.gateway.infrastructure.workpool.WorkPool;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * 事件池
 * 对应Go版本的ePool结构体
 */
@Slf4j
@Component
public class EventPool {
    
    // 事件轮询器，对应Go版本的epollers
    private final EventPoller[] eventPollers;
    
    // 工作池，对应Go版本的wPool
    private final WorkPool workPool;
    
    // 连接表，对应Go版本的table
    private final ConnectionTable connectionTable;
    
    // 连接处理回调函数，对应Go版本的f
    private BiConsumer<NIOConnection, EventPoller> connectionHandler;
    
    // 服务器套接字通道，对应Go版本的ln
    private ServerSocketChannel serverSocketChannel;
    
    // 选择器，对应Go版本的selector
    private Selector selector;
    
    // 运行标志，对应Go版本的running
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 事件轮询器数量，对应Go版本的epollerNum
    @Value("${plato.gateway.event.poller-count:4}")
    private int pollerCount;
    
    // 监听地址，对应Go版本的addr
    @Value("${plato.gateway.server.host:0.0.0.0}")
    private String host;
    
    // 监听端口，对应Go版本的port
    @Value("${plato.gateway.server.port:8080}")
    private int port;
    
    // 任务队列，对应Go版本的taskQueue
    private final ConcurrentLinkedQueue<NIOConnection> taskQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * 构造函数
     *
     * @param workPool 工作池
     * @param connectionTable 连接表
     */
    @Autowired
    public EventPool(WorkPool workPool, ConnectionTable connectionTable) {
        this.workPool = workPool;
        this.connectionTable = connectionTable;
        this.eventPollers = new EventPoller[pollerCount];
    }
    
    /**
     * 初始化事件池
     * 对应Go版本的initEpoll方法
     *
     * @param connectionHandler 连接处理回调函数
     * @throws IOException 如果初始化失败
     */
    @PostConstruct
    public void init() throws IOException {
        log.info("初始化事件池: pollerCount={}, host={}, port={}", pollerCount, host, port);
        
        // 创建服务器套接字通道
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(host, port));
        
        // 创建选择器
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        
        // 创建事件轮询器
        for (int i = 0; i < pollerCount; i++) {
            eventPollers[i] = new EventPoller(i);
        }
        
        log.info("事件池初始化完成");
    }
    
    /**
     * 设置连接处理回调函数
     * 对应Go版本的setConnectionHandler方法
     *
     * @param connectionHandler 连接处理回调函数
     */
    public void setConnectionHandler(BiConsumer<NIOConnection, EventPoller> connectionHandler) {
        this.connectionHandler = connectionHandler;
    }
    
    /**
     * 启动事件池
     * 对应Go版本的startEPool方法
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            log.info("启动事件池");
            
            // 启动事件轮询器
            for (EventPoller eventPoller : eventPollers) {
                eventPoller.start();
            }
            
            // 启动接受连接的线程
            createAcceptProcess();
            
            // 启动事件处理线程
            startEventProcess();
            
            log.info("事件池启动完成");
        }
    }
    
    /**
     * 创建接受连接的线程
     * 对应Go版本的createAcceptProcess方法
     */
    private void createAcceptProcess() {
        workPool.submit(() -> {
            log.info("启动接受连接的线程");
            
            while (running.get()) {
                try {
                    // 等待连接
                    selector.select();
                    
                    // 处理连接
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        
                        if (key.isAcceptable()) {
                            // 接受连接
                            ServerSocketChannel server = (ServerSocketChannel) key.channel();
                            SocketChannel socketChannel = server.accept();
                            
                            // 配置连接
                            socketChannel.configureBlocking(false);
                            Socket socket = socketChannel.socket();
                            socket.setTcpNoDelay(true);
                            socket.setKeepAlive(true);
                            
                            // 创建连接对象
                            NIOConnection NIOConnection = new NIOConnection();
                            NIOConnection.setSocketChannel(socketChannel);
                            NIOConnection.setRemoteAddress(socket.getRemoteSocketAddress().toString());
                            
                            // 添加到任务队列
                            taskQueue.offer(NIOConnection);
                        }
                    }
                } catch (IOException e) {
                    log.error("接受连接失败", e);
                }
            }
            
            log.info("接受连接的线程已停止");
        });
    }
    
    /**
     * 启动事件处理线程
     * 对应Go版本的startEProc方法
     */
    private void startEventProcess() {
        workPool.submit(() -> {
            log.info("启动事件处理线程");
            
            int index = 0;
            while (running.get()) {
                try {
                    // 从任务队列中获取连接
                    NIOConnection NIOConnection = taskQueue.poll();
                    if (NIOConnection != null) {
                        // 选择事件轮询器
                        EventPoller eventPoller = eventPollers[index++ % pollerCount];
                        
                        // 添加到事件轮询器
                        eventPoller.add(NIOConnection);
                        
                        // 添加到连接表
                        connectionTable.add(NIOConnection);
                        
                        // 调用连接处理回调函数
                        if (connectionHandler != null) {
                            connectionHandler.accept(NIOConnection, eventPoller);
                        }
                    } else {
                        // 如果没有连接，则休眠一段时间
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    log.error("处理连接失败", e);
                }
            }
            
            log.info("事件处理线程已停止");
        });
    }
    
    /**
     * 添加任务
     * 对应Go版本的addTask方法
     *
     * @param NIOConnection 连接对象
     */
    public void addTask(NIOConnection NIOConnection) {
        taskQueue.offer(NIOConnection);
    }
    
    /**
     * 停止事件池
     * 对应Go版本的stop方法
     */
    @PreDestroy
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("停止事件池");
            
            // 停止事件轮询器
            for (EventPoller eventPoller : eventPollers) {
                eventPoller.stop();
            }
            
            // 关闭服务器套接字通道
            try {
                if (serverSocketChannel != null) {
                    serverSocketChannel.close();
                }
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException e) {
                log.error("关闭服务器套接字通道失败", e);
            }
            
            log.info("事件池已停止");
        }
    }
} 