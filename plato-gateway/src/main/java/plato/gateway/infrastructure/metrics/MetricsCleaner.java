package plato.gateway.infrastructure.metrics;

/**
 * 监控数据清理器接口
 * 负责清理过期的监控数据
 */
public interface MetricsCleaner {
    
    /**
     * 清理过期的监控数据
     * 根据配置的保留时间，删除过期的监控数据文件
     */
    void cleanExpiredMetrics();
} 