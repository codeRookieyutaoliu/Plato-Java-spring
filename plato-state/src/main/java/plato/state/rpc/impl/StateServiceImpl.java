package plato.state.rpc.impl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plato.common.rpc.StateService;
import plato.common.rpc.StateGrpc;
import plato.common.rpc.StateRequest;
import plato.common.rpc.StateResponse;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 状态服务实现类
 */
@Slf4j
@Service
public class StateServiceImpl implements StateService {
    
    private final Map<String, ManagedChannel> channelCache = new ConcurrentHashMap<>();
    private final Map<String, StateGrpc.StateBlockingStub> clientCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // 初始化逻辑
    }
    
    @Override
    public boolean sendMessage(String endpoint, Long connectionId, byte[] messageBytes) {
        if (connectionId == null || connectionId <= 0) {
            log.warn("Invalid connection ID: {}", connectionId);
            return false;
        }
        
        if (messageBytes == null || messageBytes.length == 0) {
            log.warn("Empty message for connection: {}", connectionId);
            return false;
        }
        
        try {
            StateRequest request = StateRequest.newBuilder()
                    .setEndpoint(endpoint)
                    .setConnId(connectionId)
                    .setData(com.google.protobuf.ByteString.copyFrom(messageBytes))
                    .build();
            
            StateGrpc.StateBlockingStub client = getClient(endpoint);
            StateResponse response = client.sendMsg(request);
            
            if (response.getCode() != 0) {
                log.warn("Failed to send message to state service, connection: {}, code: {}, message: {}",
                        connectionId, response.getCode(), response.getMsg());
                return false;
            }
            
            return true;
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus(), e);
            return false;
        } catch (Exception e) {
            log.error("Error sending message to state service: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean cancelConnection(String endpoint, Long connectionId) {
        if (connectionId == null || connectionId <= 0) {
            log.warn("Invalid connection ID: {}", connectionId);
            return false;
        }
        
        try {
            StateRequest request = StateRequest.newBuilder()
                    .setEndpoint(endpoint)
                    .setConnId(connectionId)
                    .build();
            
            StateGrpc.StateBlockingStub client = getClient(endpoint);
            StateResponse response = client.cancelConn(request);
            
            if (response.getCode() != 0) {
                log.warn("Failed to cancel connection in state service, connection: {}, code: {}, message: {}",
                        connectionId, response.getCode(), response.getMsg());
                return false;
            }
            
            return true;
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {}", e.getStatus(), e);
            return false;
        } catch (Exception e) {
            log.error("Error cancelling connection in state service: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private StateGrpc.StateBlockingStub getClient(String endpoint) {
        if (!clientCache.containsKey(endpoint)) {
            createChannel(endpoint);
        }
        return clientCache.get(endpoint);
    }
    
    private void createChannel(String endpoint) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051) // 配置应该从配置文件读取
                .usePlaintext()
                .build();
        
        StateGrpc.StateBlockingStub client = StateGrpc.newBlockingStub(channel);
        
        channelCache.put(endpoint, channel);
        clientCache.put(endpoint, client);
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down gRPC channels");
        
        for (Map.Entry<String, ManagedChannel> entry : channelCache.entrySet()) {
            String endpoint = entry.getKey();
            ManagedChannel channel = entry.getValue();
            
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                log.info("gRPC channel for endpoint {} shutdown completed", endpoint);
            } catch (InterruptedException e) {
                log.warn("gRPC channel shutdown interrupted for endpoint {}", endpoint, e);
                Thread.currentThread().interrupt();
            }
        }
        
        channelCache.clear();
        clientCache.clear();
    }
} 