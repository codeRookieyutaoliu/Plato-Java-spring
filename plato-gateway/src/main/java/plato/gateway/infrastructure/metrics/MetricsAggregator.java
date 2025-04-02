package plato.gateway.infrastructure.metrics;

/**
 * 监控数据聚合器接口
 * 负责聚合监控数据
 */
public interface MetricsAggregator {
    
    /**
     * 聚合监控数据
     * 将一段时间内的监控数据聚合成一个文件
     */
    void aggregateMetrics();
} 