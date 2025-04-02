package plato.gateway.infrastructure.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * XXL-Job 任务处理器
 * 处理 XXL-Job 调度的任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class XxlJobHandler {

    private final JobHandler jobHandler;

    /**
     * 清理过期监控数据任务
     * 每小时执行一次
     */
    @XxlJob("cleanExpiredMetricsJob")
    public void cleanExpiredMetricsJob() {
        log.info("XXL-Job 执行任务: 清理过期监控数据");
        
        try {
            jobHandler.cleanExpiredMetrics();
            XxlJobHelper.handleSuccess("清理过期监控数据成功");
        } catch (Exception e) {
            log.error("清理过期监控数据失败: {}", e.getMessage(), e);
            XxlJobHelper.handleFail("清理过期监控数据失败: " + e.getMessage());
        }
    }

    /**
     * 聚合监控数据任务
     * 每分钟执行一次
     */
    @XxlJob("aggregateMetricsJob")
    public void aggregateMetricsJob() {
        log.info("XXL-Job 执行任务: 聚合监控数据");
        
        try {
            jobHandler.aggregateMetrics();
            XxlJobHelper.handleSuccess("聚合监控数据成功");
        } catch (Exception e) {
            log.error("聚合监控数据失败: {}", e.getMessage(), e);
            XxlJobHelper.handleFail("聚合监控数据失败: " + e.getMessage());
        }
    }

    /**
     * 清理空闲连接任务
     * 每分钟执行一次
     */
    @XxlJob("cleanIdleConnectionsJob")
    public void cleanIdleConnectionsJob() {
        log.info("XXL-Job 执行任务: 清理空闲连接");
        
        try {
            jobHandler.cleanIdleConnections();
            XxlJobHelper.handleSuccess("清理空闲连接成功");
        } catch (Exception e) {
            log.error("清理空闲连接失败: {}", e.getMessage(), e);
            XxlJobHelper.handleFail("清理空闲连接失败: " + e.getMessage());
        }
    }
} 