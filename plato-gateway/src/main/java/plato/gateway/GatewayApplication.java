package plato.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import plato.gateway.infrastructure.server.ITcpServer;
import plato.gateway.infrastructure.server.TcpServerSelector;

/**
 * 网关服务应用程序
 * 应用程序入口
 * 对应Go版本的main.go
 * 
 * 该应用程序提供长连接管理功能，包括：
 * 1. TCP长连接服务器（支持 Java NIO 和 Netty 两种实现）
 * 2. 连接管理
 * 3. 消息处理
 * 4. 与状态服务的gRPC通信
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"plato.gateway", "plato.common"})
@EnableDiscoveryClient
@EnableScheduling
public class GatewayApplication {
    
    /**
     * 主方法
     * 对应Go版本的main函数
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        log.info("启动网关服务");
        
        // 启动Spring应用
        ConfigurableApplicationContext context = SpringApplication.run(GatewayApplication.class, args);
        
        // 获取TCP服务器选择器
        TcpServerSelector tcpServerSelector = context.getBean(TcpServerSelector.class);
        
        // 获取当前配置的TCP服务器
        ITcpServer tcpServer = tcpServerSelector.getCurrentServer();
        
        // 启动TCP服务器
        tcpServer.start();
        
        log.info("网关服务启动完成");
    }
}