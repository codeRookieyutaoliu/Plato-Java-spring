package plato.gateway.infrastructure.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 默认监控数据聚合器
 * 实现监控数据聚合功能
 */
@Slf4j
@Component
@RefreshScope
@RequiredArgsConstructor
public class DefaultMetricsAggregator implements MetricsAggregator {

    // JSON 对象映射器
    private final ObjectMapper objectMapper;

    // 监控数据目录
    @Value("${plato.gateway.metrics.dir:./metrics}")
    private String metricsDir;

    // 是否启用监控数据聚合
    @Value("${plato.gateway.metrics.aggregation.enabled:true}")
    private boolean aggregationEnabled;

    // 聚合时间窗口（分钟）
    @Value("${plato.gateway.metrics.aggregation.window-minutes:60}")
    private int aggregationWindowMinutes;

    /**
     * 聚合监控数据
     * 将一段时间内的监控数据聚合成一个文件
     */
    @Override
    public void aggregateMetrics() {
        if (!aggregationEnabled) {
            log.info("监控数据聚合已禁用");
            return;
        }

        log.info("开始聚合监控数据: 聚合窗口={}分钟", aggregationWindowMinutes);

        File dir = new File(metricsDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                log.error("创建监控数据目录失败: {}", metricsDir);
                return;
            }
        }

        if (!dir.isDirectory()) {
            log.error("监控数据路径不是目录: {}", metricsDir);
            return;
        }

        // 获取所有文件
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            log.info("监控数据目录为空，无需聚合");
            return;
        }

        // 计算聚合窗口的开始时间
        long windowStartTimeMillis = System.currentTimeMillis() - (long) aggregationWindowMinutes * 60 * 1000;

        // 按文件类型分组
        Map<String, List<File>> fileGroups = groupFilesByType(files, windowStartTimeMillis);

        // 聚合各类型的文件
        int aggregatedCount = 0;
        for (Map.Entry<String, List<File>> entry : fileGroups.entrySet()) {
            String fileType = entry.getKey();
            List<File> fileList = entry.getValue();

            if (fileList.size() <= 1) {
                continue; // 只有一个文件不需要聚合
            }

            try {
                boolean success = aggregateFilesByType(fileType, fileList);
                if (success) {
                    aggregatedCount += fileList.size();
                }
            } catch (Exception e) {
                log.error("聚合文件类型 {} 失败: {}", fileType, e.getMessage(), e);
            }
        }

        log.info("聚合监控数据完成: 聚合文件数量={}", aggregatedCount);
    }

    /**
     * 按文件类型分组
     *
     * @param files 文件数组
     * @param windowStartTimeMillis 窗口开始时间
     * @return 分组后的文件映射
     */
    private Map<String, List<File>> groupFilesByType(File[] files, long windowStartTimeMillis) {
        Map<String, List<File>> fileGroups = new HashMap<>();

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }

            // 检查文件是否在聚合窗口内
            if (file.lastModified() < windowStartTimeMillis) {
                continue;
            }

            String fileName = file.getName();
            // 提取文件类型（如 connection_metrics, message_metrics 等）
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex <= 0) {
                continue;
            }

            String fileType = fileName.substring(0, dotIndex);
            fileGroups.computeIfAbsent(fileType, k -> new ArrayList<>()).add(file);
        }

        return fileGroups;
    }

    /**
     * 按文件类型聚合文件
     *
     * @param fileType 文件类型
     * @param files 文件列表
     * @return 是否聚合成功
     */
    private boolean aggregateFilesByType(String fileType, List<File> files) throws IOException {
        log.info("聚合文件类型: {}, 文件数量: {}", fileType, files.size());

        // 按最后修改时间排序
        files.sort(Comparator.comparingLong(File::lastModified));

        // 读取所有文件内容
        List<Map<String, Object>> allMetrics = new ArrayList<>();
        for (File file : files) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                Map<String, Object> metrics = objectMapper.readValue(content, Map.class);
                allMetrics.add(metrics);
            } catch (Exception e) {
                log.warn("读取文件失败: {}, 错误: {}", file.getName(), e.getMessage());
            }
        }

        if (allMetrics.isEmpty()) {
            log.warn("没有有效的监控数据可聚合: {}", fileType);
            return false;
        }

        // 聚合数据
        Map<String, Object> aggregatedMetrics = aggregateMetricsData(allMetrics);

        // 生成聚合文件名
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now());
        String aggregatedFileName = fileType + "_aggregated_" + timestamp + ".json";
        String aggregatedFilePath = Paths.get(metricsDir, aggregatedFileName).toString();

        // 写入聚合文件
        try {
            String aggregatedContent = objectMapper.writeValueAsString(aggregatedMetrics);
            Files.write(Paths.get(aggregatedFilePath), aggregatedContent.getBytes());
            log.info("写入聚合文件成功: {}", aggregatedFileName);

            // 删除原始文件
            for (File file : files) {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.warn("删除原始文件失败: {}", file.getName());
                }
            }

            return true;
        } catch (Exception e) {
            log.error("写入聚合文件失败: {}, 错误: {}", aggregatedFileName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 聚合监控数据
     *
     * @param metricsList 监控数据列表
     * @return 聚合后的监控数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> aggregateMetricsData(List<Map<String, Object>> metricsList) {
        Map<String, Object> result = new HashMap<>();

        // 如果只有一个数据，直接返回
        if (metricsList.size() == 1) {
            return metricsList.get(0);
        }

        // 获取所有指标名称
        Set<String> metricNames = new HashSet<>();
        for (Map<String, Object> metrics : metricsList) {
            metricNames.addAll(metrics.keySet());
        }

        // 聚合每个指标
        for (String metricName : metricNames) {
            List<Object> values = new ArrayList<>();
            
            // 收集所有值
            for (Map<String, Object> metrics : metricsList) {
                Object value = metrics.get(metricName);
                if (value != null) {
                    values.add(value);
                }
            }
            
            if (values.isEmpty()) {
                continue;
            }

            // 根据值类型进行聚合
            Object firstValue = values.get(0);
            
            if (firstValue instanceof Number) {
                // 数值类型，计算总和
                double sum = 0;
                for (Object value : values) {
                    if (value instanceof Number) {
                        sum += ((Number) value).doubleValue();
                    }
                }
                
                // 如果是整数，返回整数结果
                if (firstValue instanceof Integer || firstValue instanceof Long) {
                    result.put(metricName, (long) sum);
                } else {
                    result.put(metricName, sum);
                }
            } else if (firstValue instanceof Map) {
                // Map 类型，递归聚合
                List<Map<String, Object>> mapValues = new ArrayList<>();
                for (Object value : values) {
                    if (value instanceof Map) {
                        mapValues.add((Map<String, Object>) value);
                    }
                }
                result.put(metricName, aggregateMetricsData(mapValues));
            } else if (firstValue instanceof List) {
                // List 类型，合并列表
                List<Object> mergedList = new ArrayList<>();
                for (Object value : values) {
                    if (value instanceof List) {
                        mergedList.addAll((List<?>) value);
                    }
                }
                result.put(metricName, mergedList);
            } else {
                // 其他类型，使用最后一个值
                result.put(metricName, values.get(values.size() - 1));
            }
        }

        // 添加聚合时间戳
        result.put("aggregatedAt", System.currentTimeMillis());
        result.put("aggregatedFiles", metricsList.size());

        return result;
    }
} 