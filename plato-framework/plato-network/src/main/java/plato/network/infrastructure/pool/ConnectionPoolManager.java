package plato.network.infrastructure.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.message.application.service.MessageProcessingService;
import plato.network.domain.service.ConnectionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 连接池管理器
 * <p>
 * 管理多个连接池，提供连接池的创建、获取和销毁功能
 * </p>
 * 
 * 对应Go项目中的连接池管理器实现
 */
public class ConnectionPoolManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPoolManager.class);
    
    /**
     * 默认连接池健康检查间隔（秒）
     */
    private static final int DEFAULT_HEALTH_CHECK_INTERVAL = 60;
    
    /**
     * 连接池健康检查间隔（秒）
     */
    private final int healthCheckInterval;
    
    /**
     * 连接管理器
     */
    private final ConnectionManager connectionManager;
    
    /**
     * 消息处理服务
     */
    private final MessageProcessingService messageProcessingService;
    
    /**
     * 连接池映射表
     * 键：连接池ID，值：连接池
     */
    private final Map<String, ConnectionPool> pools;
    
    /**
     * 健康检查调度器
     */
    private final ScheduledExecutorService scheduler;
    
    /**
     * 连接池管理器是否运行
     */
    private volatile boolean running;
    
    /**
     * 构造函数
     *
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     */
    public ConnectionPoolManager(ConnectionManager connectionManager, MessageProcessingService messageProcessingService) {
        this(DEFAULT_HEALTH_CHECK_INTERVAL, connectionManager, messageProcessingService);
    }
    
    /**
     * 构造函数
     *
     * @param healthCheckInterval 连接池健康检查间隔（秒）
     * @param connectionManager 连接管理器
     * @param messageProcessingService 消息处理服务
     */
    public ConnectionPoolManager(int healthCheckInterval, ConnectionManager connectionManager, MessageProcessingService messageProcessingService) {
        this.healthCheckInterval = healthCheckInterval;
        this.connectionManager = connectionManager;
        this.messageProcessingService = messageProcessingService;
        this.pools = new ConcurrentHashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "connection-pool-health-checker");
            thread.setDaemon(true);
            return thread;
        });
        
        LOGGER.info("创建连接池管理器: 健康检查间隔={}秒", healthCheckInterval);
    }
    
    /**
     * 启动连接池管理器
     */
    public void start() {
        if (running) {
            LOGGER.warn("连接池管理器已经在运行");
            return;
        }
        
        LOGGER.info("启动连接池管理器");
        
        // 启动健康检查
        scheduler.scheduleAtFixedRate(this::healthCheck, healthCheckInterval, healthCheckInterval, TimeUnit.SECONDS);
        
        running = true;
        LOGGER.info("连接池管理器启动成功");
    }
    
    /**
     * 停止连接池管理器
     */
    public void stop() {
        if (!running) {
            LOGGER.warn("连接池管理器未运行");
            return;
        }
        
        LOGGER.info("停止连接池管理器");
        
        // 停止健康检查
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
        
        // 关闭所有连接池
        for (ConnectionPool pool : pools.values()) {
            try {
                pool.close();
            } catch (Exception e) {
                LOGGER.error("关闭连接池失败: ID={}", pool.getPoolId(), e);
            }
        }
        
        pools.clear();
        running = false;
        LOGGER.info("连接池管理器已停止");
    }
    
    /**
     * 创建连接池
     *
     * @param poolId 连接池ID
     * @param host 服务器地址
     * @param port 服务器端口
     * @return 创建的连接池
     */
    public ConnectionPool createPool(String poolId, String host, int port) {
        return createPool(poolId, host, port, 
                ConnectionPool.DEFAULT_MIN_CONNECTIONS, 
                ConnectionPool.DEFAULT_MAX_CONNECTIONS);
    }
    
    /**
     * 创建连接池
     *
     * @param poolId 连接池ID
     * @param host 服务器地址
     * @param port 服务器端口
     * @param minConnections 最小连接数
     * @param maxConnections 最大连接数
     * @return 创建的连接池
     */
    public ConnectionPool createPool(String poolId, String host, int port, 
                                    int minConnections, int maxConnections) {
        if (pools.containsKey(poolId)) {
            LOGGER.warn("连接池已存在: ID={}", poolId);
            return pools.get(poolId);
        }
        
        LOGGER.info("创建连接池: ID={}, 服务器={}:{}, 最小连接数={}, 最大连接数={}", 
                poolId, host, port, minConnections, maxConnections);
        
        // 创建连接池
        ConnectionPool pool = new ConnectionPool(poolId, host, port, minConnections, maxConnections, 
                ConnectionPool.DEFAULT_ACQUIRE_TIMEOUT, connectionManager, messageProcessingService);
        
        // 初始化连接池
        pool.initialize();
        
        // 添加到映射表
        pools.put(poolId, pool);
        
        return pool;
    }
    
    /**
     * 获取连接池
     *
     * @param poolId 连接池ID
     * @return 连接池，如果不存在则返回null
     */
    public ConnectionPool getPool(String poolId) {
        return pools.get(poolId);
    }
    
    /**
     * 移除连接池
     *
     * @param poolId 连接池ID
     */
    public void removePool(String poolId) {
        ConnectionPool pool = pools.remove(poolId);
        if (pool != null) {
            LOGGER.info("移除连接池: ID={}", poolId);
            pool.close();
        }
    }
    
    /**
     * 健康检查
     */
    private void healthCheck() {
        try {
            LOGGER.debug("执行连接池健康检查");
            
            // 检查所有连接池
            for (ConnectionPool pool : pools.values()) {
                try {
                    // 记录连接池状态
                    LOGGER.debug("连接池状态: {}", pool.getStatus());
                    
                    // 检查连接池是否已关闭
                    if (pool.isClosed()) {
                        LOGGER.warn("发现已关闭的连接池: ID={}", pool.getPoolId());
                        pools.remove(pool.getPoolId());
                    }
                } catch (Exception e) {
                    LOGGER.error("检查连接池状态异常: ID={}", pool.getPoolId(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("连接池健康检查异常", e);
        }
    }
    
    /**
     * 获取连接池数量
     *
     * @return 连接池数量
     */
    public int getPoolCount() {
        return pools.size();
    }
    
    /**
     * 获取所有连接池的状态信息
     *
     * @return 所有连接池的状态信息
     */
    public Map<String, String> getAllPoolStatus() {
        Map<String, String> status = new ConcurrentHashMap<>();
        for (ConnectionPool pool : pools.values()) {
            status.put(pool.getPoolId(), pool.getStatus());
        }
        return status;
    }
    
    /**
     * 连接池管理器是否运行
     *
     * @return 是否运行
     */
    public boolean isRunning() {
        return running;
    }
} 