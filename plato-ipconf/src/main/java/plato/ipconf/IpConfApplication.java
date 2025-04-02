package plato.ipconf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IP配置服务应用程序
 * 提供网关地址查询和负载均衡功能
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class IpConfApplication {
    /**
     * 主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(IpConfApplication.class, args);
    }
} 