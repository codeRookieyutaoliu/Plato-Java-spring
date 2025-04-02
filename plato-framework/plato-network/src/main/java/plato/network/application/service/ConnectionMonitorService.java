package plato.network.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plato.network.domain.service.ConnectionManager;
import plato.network.domain.valueobject.ConnectionStatistics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 连接监控服务
 * <p>
 * 用于定期检查和清理空闲连接，收集连接统计信息
 * </p>
 * 
 * 对应Go项目中的连接监控功能
 */
public class ConnectionMonitorService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionMonitorService.class);
    
    /**
     * 默认空闲连接超时时间（毫秒）
     */
    private static final long DEFAULT_IDLE_TIMEOUT_MILLIS = 180000; // 3分钟
    
    /**
     * 默认统计信息打印间隔（秒）
     */
    private static final long DEFAULT_STATS_INTERVAL_SECONDS = 60;
    
    /**
     * 默认清理任务间隔（秒）
     */
    private static final long DEFAULT_CLEANUP_INTERVAL_SECONDS = 60;
    
    /**
     * 连接管理器
     */
    private final ConnectionManager connectionManager;
    
    /**
     * 连接统计信息
     */
    private final ConnectionStatistics statistics;
    
    /**
     * 空闲连接超时时间（毫秒）
     */
    private final long idleTimeoutMillis;
    
    /**
     * 统计信息打印间隔（秒）
     */
    private final long statsIntervalSeconds;
    
    /**
     * 清理任务间隔（秒）
     */
    private final long cleanupIntervalSeconds;
    
    /**
     * 定时任务执行器
     */
    private final ScheduledExecutorService scheduler;
    
    /**
     * 是否已启动
     */
    private volatile boolean running;
    
    /**
     * 构造函数
     *
     * @param connectionManager 连接管理器
     * @param statistics 连接统计信息
     */
    public ConnectionMonitorService(ConnectionManager connectionManager, ConnectionStatistics statistics) {
        this(connectionManager, statistics, DEFAULT_IDLE_TIMEOUT_MILLIS, 
             DEFAULT_STATS_INTERVAL_SECONDS, DEFAULT_CLEANUP_INTERVAL_SECONDS);
    }
    
    /**
     * 构造函数
     *
     * @param connectionManager 连接管理器
     * @param statistics 连接统计信息
     * @param idleTimeoutMillis 空闲连接超时时间（毫秒）
     * @param statsIntervalSeconds 统计信息打印间隔（秒）
     * @param cleanupIntervalSeconds 清理任务间隔（秒）
     */
    public ConnectionMonitorService(ConnectionManager connectionManager, ConnectionStatistics statistics,
                                  long idleTimeoutMillis, long statsIntervalSeconds, long cleanupIntervalSeconds) {
        this.connectionManager = connectionManager;
        this.statistics = statistics;
        this.idleTimeoutMillis = idleTimeoutMillis;
        this.statsIntervalSeconds = statsIntervalSeconds;
        this.cleanupIntervalSeconds = cleanupIntervalSeconds;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.running = false;
    }
    
    /**
     * 启动监控服务
     */
    public void start() {
        if (running) {
            LOGGER.warn("连接监控服务已经在运行");
            return;
        }
        
        LOGGER.info("启动连接监控服务");
        
        // 定时清理空闲连接
        scheduler.scheduleAtFixedRate(this::cleanupIdleConnections, 
                cleanupIntervalSeconds, cleanupIntervalSeconds, TimeUnit.SECONDS);
        
        // 定时打印统计信息
        scheduler.scheduleAtFixedRate(this::logStatistics,
                statsIntervalSeconds, statsIntervalSeconds, TimeUnit.SECONDS);
        
        running = true;
    }
    
    /**
     * 停止监控服务
     */
    public void stop() {
        if (!running) {
            LOGGER.warn("连接监控服务未运行");
            return;
        }
        
        LOGGER.info("停止连接监控服务");
        
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.error("停止连接监控服务时发生异常", e);
            Thread.currentThread().interrupt();
        } finally {
            running = false;
        }
    }
    
    /**
     * 清理空闲连接
     */
    private void cleanupIdleConnections() {
        try {
            LOGGER.debug("开始清理空闲连接, 超时时间={}ms", idleTimeoutMillis);
            
            int count = connectionManager.cleanInactiveConnections(idleTimeoutMillis);
            
            if (count > 0) {
                LOGGER.info("清理了{}个空闲连接", count);
            } else {
                LOGGER.debug("没有空闲连接需要清理");
            }
        } catch (Exception e) {
            LOGGER.error("清理空闲连接时发生异常", e);
        }
    }
    
    /**
     * 打印统计信息
     */
    private void logStatistics() {
        try {
            LOGGER.info("连接统计信息: {}", statistics);
            
            // 更新活跃连接数和已认证连接数的统计
            int connectionCount = connectionManager.countConnections();
            int authenticatedConnectionCount = connectionManager.countAuthenticatedConnections();
            
            // 这里不使用statistics的incrementXXX方法，因为这些方法是增量的，而我们需要设置为实际值
            // 这里应该有一个更好的方式来更新这些值，但为了简单起见，我们暂时这样处理
            while (statistics.getActiveConnections() < connectionCount) {
                statistics.incrementActiveConnections();
            }
            while (statistics.getActiveConnections() > connectionCount) {
                statistics.decrementActiveConnections();
            }
            
            while (statistics.getAuthenticatedConnections() < authenticatedConnectionCount) {
                statistics.incrementAuthenticatedConnections();
            }
            while (statistics.getAuthenticatedConnections() > authenticatedConnectionCount) {
                statistics.decrementAuthenticatedConnections();
            }
        } catch (Exception e) {
            LOGGER.error("打印统计信息时发生异常", e);
        }
    }
    
    /**
     * 获取空闲连接超时时间（毫秒）
     *
     * @return 空闲连接超时时间（毫秒）
     */
    public long getIdleTimeoutMillis() {
        return idleTimeoutMillis;
    }
    
    /**
     * 获取统计信息打印间隔（秒）
     *
     * @return 统计信息打印间隔（秒）
     */
    public long getStatsIntervalSeconds() {
        return statsIntervalSeconds;
    }
    
    /**
     * 获取清理任务间隔（秒）
     *
     * @return 清理任务间隔（秒）
     */
    public long getCleanupIntervalSeconds() {
        return cleanupIntervalSeconds;
    }
    
    /**
     * 是否已启动
     *
     * @return 是否已启动
     */
    public boolean isRunning() {
        return running;
    }
} 