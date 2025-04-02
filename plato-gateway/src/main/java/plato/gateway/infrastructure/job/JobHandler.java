package plato.gateway.infrastructure.job;

/**
 * 任务处理器接口
 * 定义任务处理方法
 */
public interface JobHandler {
    
    /**
     * 清理过期监控数据
     */
    void cleanExpiredMetrics();
    
    /**
     * 聚合监控数据
     */
    void aggregateMetrics();
    
    /**
     * 清理空闲连接
     */
    void cleanIdleConnections();
} 