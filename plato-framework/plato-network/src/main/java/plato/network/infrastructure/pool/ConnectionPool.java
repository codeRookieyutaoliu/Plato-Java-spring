package plato.network.infrastructure.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.application.service.MessageProcessingService;
import plato.network.domain.service.ConnectionManager;
import plato.network.domain.valueobject.ConnectionStatistics;
import plato.network.infrastructure.netty.DefaultConnectionManager;
import plato.network.infrastructure.netty.NettyClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 连接池
 * <p>
 * 管理到特定服务器的多个连接，提供连接池功能
 * </p>
 * 
 * 对应Go项目中的连接池实现
 */
public class ConnectionPool {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);
    
    /**
     * 默认最小连接数
     */
    private static final int DEFAULT_MIN_CONNECTIONS = 2;
    
    /**
     * 默认最大连接数
     */
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    
    /**
     * 默认获取连接超时时间（毫秒）
     */
    private static final long DEFAULT_ACQUIRE_TIMEOUT = 5000;
    
    /**
     * 连接池ID
     */
    private final String poolId;
    
    /**
     * 服务器地址
     */
    private final String host;
    
    /**
     * 服务器端口
     */
    private final int port;
    
    /**
     * 最小连接数
     */
    private final int minConnections;
    
    /**
     * 最大连接数
     */
    private final int maxConnections;
    
    /**
     * 获取连接超时时间（毫秒）
     */
    private final long acquireTimeout;
    
    /**
     * 连接管理器
     */
    private final ConnectionManager connectionManager;
    
    /**
     * 消息处理服务
     */
    private final MessageProcessingService messageProcessingService;
    
    /**
     * 空闲连接队列
     */
    private final BlockingQueue<NettyClient> idleConnections;
    
    /**
     * 活跃连接列表
     */
    private final List<NettyClient> activeConnections;
    
    /**
     * 连接计数器
     */
    private final AtomicInteger connectionCount;
    
    /**
     * 连接池是否关闭
     */
    private final AtomicBoolean closed;
    
    /**
     * 连接池统计信息
     */
    private final ConnectionStatistics statistics;
    
    /**
     * 构造函数
     *
     * @param poolId 连接池ID
     * @param host 服务器地址
     * @param port 服务器端口
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     */
    public ConnectionPool(String poolId, String host, int port, 
                          ConnectionManager connectionManager, 
                          MessageProcessingService messageProcessingService) {
        this(poolId, host, port, DEFAULT_MIN_CONNECTIONS, DEFAULT_MAX_CONNECTIONS, 
             DEFAULT_ACQUIRE_TIMEOUT, connectionManager, messageProcessingService);
    }
    
    /**
     * 构造函数
     *
     * @param poolId 连接池ID
     * @param host 服务器地址
     * @param port 服务器端口
     * @param minConnections 最小连接数
     * @param maxConnections 最大连接数
     * @param acquireTimeout 获取连接超时时间（毫秒）
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     */
    public ConnectionPool(String poolId, String host, int port, int minConnections, int maxConnections, 
                          long acquireTimeout, ConnectionManager connectionManager, 
                          MessageProcessingService messageProcessingService) {
        this.poolId = poolId;
        this.host = host;
        this.port = port;
        this.minConnections = Math.max(1, minConnections);
        this.maxConnections = Math.max(this.minConnections, maxConnections);
        this.acquireTimeout = acquireTimeout;
        this.connectionManager = connectionManager;
        this.messageProcessingService = messageProcessingService;
        this.idleConnections = new ArrayBlockingQueue<>(this.maxConnections);
        this.activeConnections = new ArrayList<>(this.maxConnections);
        this.connectionCount = new AtomicInteger(0);
        this.closed = new AtomicBoolean(false);
        
        // 使用默认的连接统计信息
        if (connectionManager instanceof DefaultConnectionManager) {
            this.statistics = ((DefaultConnectionManager) connectionManager).getStatistics();
        } else {
            this.statistics = new ConnectionStatistics();
        }
        
        LOGGER.info("创建连接池: ID={}, 服务器={}:{}, 最小连接数={}, 最大连接数={}", 
                poolId, host, port, this.minConnections, this.maxConnections);
    }
    
    /**
     * 初始化连接池
     */
    public void initialize() {
        if (closed.get()) {
            throw new IllegalStateException("连接池已关闭");
        }
        
        LOGGER.info("初始化连接池: ID={}, 创建{}个初始连接", poolId, minConnections);
        
        // 创建初始连接
        for (int i = 0; i < minConnections; i++) {
            NettyClient client = createConnection();
            if (client != null) {
                idleConnections.add(client);
            }
        }
        
        LOGGER.info("连接池初始化完成: ID={}, 空闲连接数={}", poolId, idleConnections.size());
    }
    
    /**
     * 获取连接
     *
     * @return 客户端连接，如果没有可用连接则返回null
     */
    public NettyClient acquireConnection() {
        return acquireConnection(acquireTimeout);
    }
    
    /**
     * 获取连接
     *
     * @param timeout 超时时间（毫秒）
     * @return 客户端连接，如果没有可用连接则返回null
     */
    public NettyClient acquireConnection(long timeout) {
        if (closed.get()) {
            throw new IllegalStateException("连接池已关闭");
        }
        
        long startTime = System.currentTimeMillis();
        long remainingTime = timeout;
        
        while (remainingTime > 0) {
            try {
                // 从空闲队列中获取连接
                NettyClient client = idleConnections.poll(remainingTime, TimeUnit.MILLISECONDS);
                
                if (client != null) {
                    // 检查连接是否有效
                    if (client.isRunning()) {
                        synchronized (activeConnections) {
                            activeConnections.add(client);
                        }
                        LOGGER.debug("获取连接成功: 池ID={}, 服务器={}:{}", poolId, host, port);
                        return client;
                    } else {
                        // 连接无效，关闭并创建新连接
                        LOGGER.warn("获取到无效连接，关闭并创建新连接: 池ID={}, 服务器={}:{}", poolId, host, port);
                        closeConnection(client);
                        client = createConnection();
                        if (client != null) {
                            synchronized (activeConnections) {
                                activeConnections.add(client);
                            }
                            LOGGER.debug("创建新连接成功: 池ID={}, 服务器={}:{}", poolId, host, port);
                            return client;
                        }
                    }
                } else if (connectionCount.get() < maxConnections) {
                    // 没有空闲连接但未达到最大连接数，创建新连接
                    LOGGER.debug("没有空闲连接，创建新连接: 池ID={}, 服务器={}:{}", poolId, host, port);
                    client = createConnection();
                    if (client != null) {
                        synchronized (activeConnections) {
                            activeConnections.add(client);
                        }
                        LOGGER.debug("创建新连接成功: 池ID={}, 服务器={}:{}", poolId, host, port);
                        return client;
                    }
                }
                
                // 计算剩余时间
                remainingTime = timeout - (System.currentTimeMillis() - startTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("获取连接被中断: 池ID={}, 服务器={}:{}", poolId, host, port);
                return null;
            }
        }
        
        LOGGER.warn("获取连接超时: 池ID={}, 服务器={}:{}, 超时时间={}毫秒", poolId, host, port, timeout);
        return null;
    }
    
    /**
     * 释放连接
     *
     * @param client 要释放的客户端连接
     */
    public void releaseConnection(NettyClient client) {
        if (client == null) {
            return;
        }
        
        if (closed.get()) {
            closeConnection(client);
            return;
        }
        
        synchronized (activeConnections) {
            activeConnections.remove(client);
        }
        
        // 检查连接是否有效
        if (client.isRunning()) {
            // 将连接放回空闲队列
            boolean offered = idleConnections.offer(client);
            if (!offered) {
                // 空闲队列已满，关闭连接
                LOGGER.debug("空闲队列已满，关闭连接: 池ID={}, 服务器={}:{}", poolId, host, port);
                closeConnection(client);
            } else {
                LOGGER.debug("释放连接到空闲队列: 池ID={}, 服务器={}:{}", poolId, host, port);
            }
        } else {
            // 连接无效，关闭连接
            LOGGER.debug("释放的连接无效，关闭连接: 池ID={}, 服务器={}:{}", poolId, host, port);
            closeConnection(client);
        }
    }
    
    /**
     * 创建新连接
     *
     * @return 新创建的客户端连接，如果创建失败则返回null
     */
    private NettyClient createConnection() {
        if (connectionCount.get() >= maxConnections) {
            LOGGER.warn("已达到最大连接数，无法创建新连接: 池ID={}, 服务器={}:{}", poolId, host, port);
            return null;
        }
        
        LOGGER.debug("创建新连接: 池ID={}, 服务器={}:{}", poolId, host, port);
        
        // 创建新的客户端
        NettyClient client = new NettyClient(host, port, connectionManager, messageProcessingService);
        
        // 连接服务器
        boolean success = client.connect();
        if (success) {
            connectionCount.incrementAndGet();
            LOGGER.debug("创建连接成功: 池ID={}, 服务器={}:{}, 当前连接数={}", 
                    poolId, host, port, connectionCount.get());
            return client;
        } else {
            LOGGER.error("创建连接失败: 池ID={}, 服务器={}:{}", poolId, host, port);
            return null;
        }
    }
    
    /**
     * 关闭连接
     *
     * @param client 要关闭的客户端连接
     */
    private void closeConnection(NettyClient client) {
        if (client != null) {
            try {
                client.disconnect();
                connectionCount.decrementAndGet();
                LOGGER.debug("关闭连接: 池ID={}, 服务器={}:{}, 当前连接数={}", 
                        poolId, host, port, connectionCount.get());
            } catch (Exception e) {
                LOGGER.error("关闭连接异常: 池ID={}, 服务器={}:{}", poolId, host, port, e);
            }
        }
    }
    
    /**
     * 关闭连接池
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            LOGGER.info("关闭连接池: ID={}, 服务器={}:{}", poolId, host, port);
            
            // 关闭所有空闲连接
            NettyClient client;
            while ((client = idleConnections.poll()) != null) {
                closeConnection(client);
            }
            
            // 关闭所有活跃连接
            synchronized (activeConnections) {
                for (NettyClient activeClient : activeConnections) {
                    closeConnection(activeClient);
                }
                activeConnections.clear();
            }
            
            LOGGER.info("连接池已关闭: ID={}, 服务器={}:{}", poolId, host, port);
        }
    }
    
    /**
     * 获取随机连接
     * 
     * @return 随机连接，如果没有可用连接则返回null
     */
    public NettyClient getRandomConnection() {
        if (closed.get()) {
            throw new IllegalStateException("连接池已关闭");
        }
        
        // 获取当前活跃连接数
        int size;
        synchronized (activeConnections) {
            size = activeConnections.size();
        }
        
        if (size == 0) {
            // 没有活跃连接，创建新连接
            return acquireConnection();
        } else {
            // 从活跃连接中随机选择一个
            synchronized (activeConnections) {
                int index = ThreadLocalRandom.current().nextInt(size);
                return activeConnections.get(index);
            }
        }
    }
    
    /**
     * 获取连接池状态信息
     * 
     * @return 连接池状态信息
     */
    public String getStatus() {
        int idle = idleConnections.size();
        int active;
        synchronized (activeConnections) {
            active = activeConnections.size();
        }
        int total = connectionCount.get();
        
        return String.format("ConnectionPool[ID=%s, 服务器=%s:%d, 空闲=%d, 活跃=%d, 总计=%d, 最大=%d]", 
                poolId, host, port, idle, active, total, maxConnections);
    }
    
    /**
     * 获取连接池ID
     * 
     * @return 连接池ID
     */
    public String getPoolId() {
        return poolId;
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
     * 获取当前连接数
     * 
     * @return 当前连接数
     */
    public int getConnectionCount() {
        return connectionCount.get();
    }
    
    /**
     * 获取空闲连接数
     * 
     * @return 空闲连接数
     */
    public int getIdleCount() {
        return idleConnections.size();
    }
    
    /**
     * 获取活跃连接数
     * 
     * @return 活跃连接数
     */
    public int getActiveCount() {
        synchronized (activeConnections) {
            return activeConnections.size();
        }
    }
    
    /**
     * 获取最大连接数
     * 
     * @return 最大连接数
     */
    public int getMaxConnections() {
        return maxConnections;
    }
    
    /**
     * 获取连接统计信息
     * 
     * @return 连接统计信息
     */
    public ConnectionStatistics getStatistics() {
        return statistics;
    }
    
    /**
     * 连接池是否已关闭
     * 
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return closed.get();
    }
} 