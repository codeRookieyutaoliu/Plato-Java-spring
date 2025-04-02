package plato.gateway.infrastructure.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;

/**
 * 默认监控数据清理器
 * 实现监控数据清理功能
 */
@Slf4j
@Component
@RefreshScope
public class DefaultMetricsCleaner implements MetricsCleaner {

    // 监控数据目录
    @Value("${plato.gateway.metrics.dir:./metrics}")
    private String metricsDir;

    // 监控数据保留天数
    @Value("${plato.gateway.metrics.retention-days:7}")
    private int retentionDays;

    /**
     * 清理过期的监控数据
     * 根据配置的保留时间，删除过期的监控数据文件
     */
    @Override
    public void cleanExpiredMetrics() {
        log.info("开始清理过期监控数据: 保留天数={}", retentionDays);
        
        File dir = new File(metricsDir);
        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("监控数据目录不存在: {}", metricsDir);
            return;
        }
        
        // 计算过期时间点
        long expireTimeMillis = System.currentTimeMillis() - (long) retentionDays * 24 * 60 * 60 * 1000;
        LocalDateTime expireDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(expireTimeMillis), ZoneId.systemDefault());
        
        // 获取所有文件
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            log.info("监控数据目录为空，无需清理");
            return;
        }
        
        int deletedCount = 0;
        
        // 遍历文件，删除过期文件
        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            
            // 获取文件最后修改时间
            long lastModified = file.lastModified();
            LocalDateTime fileDateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(lastModified), ZoneId.systemDefault());
            
            // 如果文件时间早于过期时间，则删除
            if (fileDateTime.isBefore(expireDateTime)) {
                String fileName = file.getName();
                boolean deleted = file.delete();
                
                if (deleted) {
                    log.info("删除过期监控数据文件: {}, 文件时间: {}", fileName, fileDateTime);
                    deletedCount++;
                } else {
                    log.warn("删除过期监控数据文件失败: {}", fileName);
                }
            }
        }
        
        log.info("清理过期监控数据完成: 删除文件数量={}", deletedCount);
    }
} 