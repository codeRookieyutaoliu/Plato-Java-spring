package plato.gateway.infrastructure.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 任务处理器适配器
 * 用于将 Spring Scheduling 任务适配到 XXL-Job
 */
@Slf4j
@Component
@RefreshScope
public class JobHandlerAdapter {
    
    // 任务处理器
    private final JobHandler jobHandler;
    
    // 是否启用 XXL-Job
    @Value("${xxl.job.enabled:true}")
    private boolean xxlJobEnabled;
    
    /**
     * 构造函数
     *
     * @param jobHandler 任务处理器
     */
    @Autowired
    public JobHandlerAdapter(JobHandler jobHandler) {
        this.jobHandler = jobHandler;
    }
    
    /**
     * 清理过期监控数据
     */
    @XxlJob("cleanExpiredMetricsJobHandler")
    public void cleanExpiredMetrics() {
        if (!xxlJobEnabled) {
            log.info("XXL-Job 未启用，跳过清理过期监控数据");
            return;
        }
        
        log.info("开始清理过期监控数据");
        jobHandler.cleanExpiredMetrics();
        log.info("清理过期监控数据完成");
    }
    
    /**
     * 聚合监控数据
     */
    @XxlJob("aggregateMetricsJobHandler")
    public void aggregateMetrics() {
        if (!xxlJobEnabled) {
            log.info("XXL-Job 未启用，跳过聚合监控数据");
            return;
        }
        
        log.info("开始聚合监控数据");
        jobHandler.aggregateMetrics();
        log.info("聚合监控数据完成");
    }
    
    /**
     * 清理空闲连接
     */
    @XxlJob("cleanIdleConnectionsJobHandler")
    public void cleanIdleConnections() {
        if (!xxlJobEnabled) {
            log.info("XXL-Job 未启用，跳过清理空闲连接");
            return;
        }
        
        log.info("开始清理空闲连接");
        jobHandler.cleanIdleConnections();
        log.info("清理空闲连接完成");
    }
} 