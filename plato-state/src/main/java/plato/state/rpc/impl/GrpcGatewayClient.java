package plato.state.rpc.impl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import plato.gateway.rpc.service.GatewayGrpc;
import plato.gateway.rpc.service.GatewayRequest;
import plato.gateway.rpc.service.GatewayResponse;
import plato.state.rpc.GatewayClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * gRPC网关客户端
 * 使用gRPC与网关服务通信
 * 对应Go版本中的state/rpc/client/gateway.go文件
 */
@Slf4j
@Component
public class GrpcGatewayClient implements GatewayClient {

    /**
     * 网关服务地址
     * 对应Go版本中的config.GetStateServerGatewayServerEndpoint()
     */
    @Value("${state.gateway.server.endpoint:127.0.0.1:8902}")
    private String gatewayServerEndpoint;

    /**
     * gRPC通道
     */
    private ManagedChannel channel;

    /**
     * 网关服务存根
     * 对应Go版本中的gatewayClient
     */
    private GatewayGrpc.GatewayBlockingStub gatewayClient;

    /**
     * 初始化
     * 对应Go版本中的initGatewayClient函数
     */
    @PostConstruct
    public void init() {
        try {
            // 创建gRPC通道
            channel = ManagedChannelBuilder.forTarget(gatewayServerEndpoint)
                    .usePlaintext()
                    .build();
            
            // 创建网关服务存根
            gatewayClient = GatewayGrpc.newBlockingStub(channel);
            
            log.info("Gateway client initialized with endpoint: {}", gatewayServerEndpoint);
        } catch (Exception e) {
            log.error("Error initializing Gateway client", e);
            throw new RuntimeException("Failed to initialize Gateway client", e);
        }
    }

    /**
     * 删除连接
     * 对应Go版本中的DelConn函数
     *
     * @param connId  连接ID
     * @param payload 负载数据
     * @return 是否成功
     */
    @Override
    public boolean delConn(long connId, byte[] payload) {
        try {
            // 构建请求
            GatewayRequest.Builder requestBuilder = GatewayRequest.newBuilder()
                    .setConnId(connId);
            
            if (payload != null) {
                requestBuilder.setData(com.google.protobuf.ByteString.copyFrom(payload));
            }
            
            // 设置超时时间
            GatewayGrpc.GatewayBlockingStub stub = gatewayClient.withDeadlineAfter(100, TimeUnit.MILLISECONDS);
            
            // 发送请求
            GatewayResponse response = stub.delConn(requestBuilder.build());
            
            // 检查响应
            boolean success = response.getCode() == 0;
            if (success) {
                log.debug("Successfully deleted connection: connId={}", connId);
            } else {
                log.warn("Failed to delete connection: connId={}, code={}, msg={}", 
                        connId, response.getCode(), response.getMsg());
            }
            
            return success;
        } catch (Exception e) {
            log.error("Error deleting connection: connId={}", connId, e);
            return false;
        }
    }

    /**
     * 推送消息
     * 对应Go版本中的Push函数
     *
     * @param connId  连接ID
     * @param payload 负载数据
     * @return 是否成功
     */
    @Override
    public boolean push(long connId, byte[] payload) {
        try {
            // 构建请求
            GatewayRequest request = GatewayRequest.newBuilder()
                    .setConnId(connId)
                    .setData(com.google.protobuf.ByteString.copyFrom(payload))
                    .build();
            
            // 设置超时时间
            GatewayGrpc.GatewayBlockingStub stub = gatewayClient.withDeadlineAfter(100, TimeUnit.MILLISECONDS);
            
            // 发送请求
            GatewayResponse response = stub.push(request);
            
            // 检查响应
            boolean success = response.getCode() == 0;
            if (success) {
                log.debug("Successfully pushed message: connId={}, payloadSize={}", 
                        connId, payload.length);
            } else {
                log.warn("Failed to push message: connId={}, code={}, msg={}", 
                        connId, response.getCode(), response.getMsg());
            }
            
            return success;
        } catch (Exception e) {
            log.error("Error pushing message: connId={}", connId, e);
            return false;
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("Gateway client shutdown");
            } catch (InterruptedException e) {
                log.error("Error shutting down Gateway client", e);
                Thread.currentThread().interrupt();
            }
        }
    }
} 