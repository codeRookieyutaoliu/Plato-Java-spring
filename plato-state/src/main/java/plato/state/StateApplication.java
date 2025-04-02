package plato.state;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import plato.state.rpc.client.GatewayClient;
import plato.state.rpc.service.StateService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * State应用程序
 * 长连接状态管理服务的入口类
 * 对应Go版本中的state/server.go文件中的RunMain函数
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"plato.common", "plato.state"})
@RequiredArgsConstructor
public class StateApplication {

    /**
     * gRPC服务器端口
     * 对应Go版本中的config.GetSateServerPort()
     */
    @Value("${state.service.rpc.port:8903}")
    private int rpcPort;

    /**
     * State服务
     * 对应Go版本中的cs.server
     */
    private final StateService stateService;

    /**
     * Gateway客户端
     * 对应Go版本中的client包
     */
    private final GatewayClient gatewayClient;

    /**
     * gRPC服务器
     */
    private Server grpcServer;

    /**
     * 主方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(StateApplication.class, args);
    }

    /**
     * 初始化
     * 对应Go版本中的RunMain函数
     */
    @PostConstruct
    public void init() {
        log.info("State application initializing");
    }

    /**
     * 创建gRPC服务器
     * 对应Go版本中的RunMain函数中的RPC服务器启动部分
     *
     * @return gRPC服务器
     * @throws IOException 如果启动服务器时发生IO异常
     */
    @Bean
    public Server grpcServer() throws IOException {
        // 创建gRPC服务器
        grpcServer = ServerBuilder.forPort(rpcPort)
                .addService(stateService)
                .build()
                .start();
        
        log.info("gRPC server started on port: {}", rpcPort);
        
        // 添加JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server");
            if (grpcServer != null) {
                grpcServer.shutdown();
            }
        }));
        
        return grpcServer;
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        if (grpcServer != null) {
            grpcServer.shutdown();
            log.info("gRPC server shutdown");
        }
    }
} 