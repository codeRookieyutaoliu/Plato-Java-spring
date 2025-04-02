package plato.gateway.infrastructure.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import plato.gateway.infrastructure.connection.NIOConnection;
import plato.gateway.infrastructure.connection.IConnection;
import plato.gateway.infrastructure.connection.ConnectionIdGenerator;
import plato.gateway.infrastructure.connection.ConnectionTable;
import plato.gateway.infrastructure.event.EventPool;
import plato.gateway.infrastructure.event.EventPoller;
import plato.gateway.infrastructure.workpool.WorkPool;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP服务器
 * 基于 Java NIO 的实现
 * 对应Go版本的server.go
 */
@Slf4j
@Component
@Qualifier("nioTcpServer")
public class TcpServer implements ITcpServer {
    
    // 事件池，对应Go版本的epool
    private final EventPool eventPool;
    
    // 工作池，对应Go版本的wPool
    private final WorkPool workPool;
    
    // 连接表，对应Go版本的table
    private final ConnectionTable connectionTable;
    
    // 连接ID生成器，对应Go版本的connIDGenerater
    private final ConnectionIdGenerator connectionIdGenerator;
    
    // 运行标志，对应Go版本的running
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 最大空闲时间（秒），对应Go版本的maxIdleTime
    @Value("${plato.gateway.connection.max-idle-time:300}")
    private int maxIdleTime;
    
    // 清理间隔（秒），对应Go版本的cleanInterval
    @Value("${plato.gateway.connection.clean-interval:60}")
    private int cleanInterval;
    
    /**
     * 构造函数
     *
     * @param eventPool           事件池
     * @param workPool            工作池
     * @param connectionTable     连接表
     * @param connectionIdGenerator 连接ID生成器
     */
    @Autowired
    public TcpServer(EventPool eventPool, WorkPool workPool, ConnectionTable connectionTable, ConnectionIdGenerator connectionIdGenerator) {
        this.eventPool = eventPool;
        this.workPool = workPool;
        this.connectionTable = connectionTable;
        this.connectionIdGenerator = connectionIdGenerator;
    }
    
    /**
     * 初始化服务器
     * 对应Go版本的init方法
     *
     * @throws IOException 如果初始化失败
     */
    @PostConstruct
    @Override
    public void init() throws IOException {
        log.info("初始化TCP服务器: maxIdleTime={}s, cleanInterval={}s", maxIdleTime, cleanInterval);
        
        // 设置连接处理回调函数
        eventPool.setConnectionHandler(this::handleConnection);
        
        log.info("TCP服务器初始化完成");
    }
    
    /**
     * 启动服务器
     * 对应Go版本的RunMain方法
     */
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            log.info("启动TCP服务器");
            
            // 启动事件池
            eventPool.start();
            
            // 启动连接清理任务
            startCleanTask();
            
            log.info("TCP服务器启动完成");
        }
    }
    
    /**
     * 处理连接
     * 对应Go版本的runProc方法
     *
     * @param NIOConnection 连接对象
     * @param eventPoller 事件轮询器
     */
    private void handleConnection(NIOConnection NIOConnection, EventPoller eventPoller) {
        try {
            // 生成连接ID
            long connectionId = connectionIdGenerator.nextId();
            NIOConnection.setId(connectionId);
            NIOConnection.setEpollerId(eventPoller.getId());
            
            log.info("新连接: id={}, remoteAddress={}", connectionId, NIOConnection.getRemoteAddress());
            
            // 提交到工作池处理
            workPool.submit(() -> processConnection(NIOConnection));
        } catch (Exception e) {
            log.error("处理连接失败", e);
            eventPoller.remove(NIOConnection);
            NIOConnection.close();
        }
    }
    
    /**
     * 处理连接
     * 对应Go版本的processConnection方法
     *
     * @param NIOConnection 连接对象
     */
    private void processConnection(NIOConnection NIOConnection) {
        // 这里可以添加连接处理逻辑
        // 例如：认证、协议解析等
        
        log.debug("处理连接: id={}", NIOConnection.getId());
    }
    
    /**
     * 启动连接清理任务
     * 对应Go版本的startCleanTask方法
     */
    private void startCleanTask() {
        workPool.submit(() -> {
            log.info("启动连接清理任务: interval={}s", cleanInterval);
            
            while (running.get()) {
                try {
                    // 休眠一段时间
                    Thread.sleep(cleanInterval * 1000L);
                    
                    // 清理空闲连接
                    cleanIdleConnections();
                } catch (InterruptedException e) {
                    log.error("连接清理任务被中断", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("清理空闲连接失败", e);
                }
            }
            
            log.info("连接清理任务已停止");
        });
    }
    
    /**
     * 清理空闲连接
     * 对应Go版本的cleanIdleConnections方法
     */
    private void cleanIdleConnections() {
        log.debug("清理空闲连接: maxIdleTime={}s", maxIdleTime);
        
        int cleanedCount = 0;
        for (IConnection connection : connectionTable.getAll()) {
            if (connection.isIdleTimeout(maxIdleTime)) {
                log.info("关闭空闲连接: id={}, remoteAddress={}, idleTime={}s",
                        connection.getId(), connection.getRemoteAddress(),
                        (System.currentTimeMillis() - connection.getLastActiveTime().toEpochMilli()) / 1000);
                
                // 从连接表中删除
                connectionTable.remove(connection.getId());
                
                // 关闭连接
                connection.close();
                
                cleanedCount++;
            }
        }
        
        if (cleanedCount > 0) {
            log.info("清理空闲连接完成: 清理数量={}, 剩余连接数={}", cleanedCount, connectionTable.size());
        }
    }
    
    /**
     * 关闭连接
     * 对应Go版本的closeConn方法
     *
     * @param connectionId 连接ID
     * @return 是否成功
     */
    @Override
    public boolean closeConnection(long connectionId) {
        IConnection connection = connectionTable.get(connectionId);
        if (connection != null) {
            log.info("关闭连接: id={}, remoteAddress={}", connectionId, connection.getRemoteAddress());
            
            // 从连接表中删除
            connectionTable.remove(connectionId);
            
            // 关闭连接
            connection.close();
            
            return true;
        } else {
            log.warn("连接不存在: id={}", connectionId);
            return false;
        }
    }
    
    /**
     * 发送消息
     * 对应Go版本的sendMsgByCmd方法
     *
     * @param connectionId 连接ID
     * @param payload      消息内容
     * @return 是否成功
     */
    @Override
    public boolean sendMessage(long connectionId, byte[] payload) {
        IConnection connection = connectionTable.get(connectionId);
        if (connection != null) {
            log.debug("发送消息: id={}, length={}", connectionId, payload.length);
            return connection.sendMessage(payload);
        } else {
            log.warn("连接不存在: id={}", connectionId);
            return false;
        }
    }
    
    /**
     * 停止服务器
     * 对应Go版本的stop方法
     */
    @PreDestroy
    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("停止TCP服务器");
            
            // 停止事件池
            eventPool.stop();
            
            // 清空连接表
            connectionTable.clear();
            
            log.info("TCP服务器已停止");
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