package plato.gateway.infrastructure.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import plato.gateway.infrastructure.connection.IConnection;
import plato.gateway.infrastructure.connection.ConnectionTable;
import plato.gateway.infrastructure.metrics.MetricsAggregator;
import plato.gateway.infrastructure.metrics.MetricsCleaner;

import java.util.List;

/**
 * 默认任务处理器
 * 实现任务处理方法
 */
@Slf4j
@Component
@RefreshScope
public class DefaultJobHandler implements JobHandler {
    
    // 连接表
    private final ConnectionTable connectionTable;
    
    // 监控数据清理器
    private final MetricsCleaner metricsCleaner;
    
    // 监控数据聚合器
    private final MetricsAggregator metricsAggregator;
    
    // 最大空闲时间（秒）
    @Value("${connection.manager.max-idle-time-seconds:300}")
    private int maxIdleTimeSeconds;
    
    /**
     * 构造函数
     *
     * @param connectionTable 连接表
     * @param metricsCleaner 监控数据清理器
     * @param metricsAggregator 监控数据聚合器
     */
    @Autowired
    public DefaultJobHandler(ConnectionTable connectionTable, 
                            MetricsCleaner metricsCleaner, 
                            MetricsAggregator metricsAggregator) {
        this.connectionTable = connectionTable;
        this.metricsCleaner = metricsCleaner;
        this.metricsAggregator = metricsAggregator;
    }
    
    /**
     * 清理过期监控数据
     * 每小时执行一次
     */
    @Override
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredMetrics() {
        log.info("清理过期监控数据");
        metricsCleaner.cleanExpiredMetrics();
    }
    
    /**
     * 聚合监控数据
     * 每分钟执行一次
     */
    @Override
    @Scheduled(fixedRate = 60000)
    public void aggregateMetrics() {
        log.info("聚合监控数据");
        metricsAggregator.aggregateMetrics();
    }
    
    /**
     * 清理空闲连接
     * 每分钟执行一次
     */
    @Override
    @Scheduled(fixedRate = 60000)
    public void cleanIdleConnections() {
        log.info("清理空闲连接: maxIdleTime={}s", maxIdleTimeSeconds);
        
        int cleanedCount = 0;
        List<IConnection> connections = connectionTable.getAll();
        
        for (IConnection connection : connections) {
            if (connection.isIdleTimeout(maxIdleTimeSeconds)) {
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
} 