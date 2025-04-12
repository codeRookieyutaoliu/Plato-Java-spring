package plato.protocol.transport;

import plato.protocol.codec.tcp.MessageDecoder;
import plato.protocol.codec.tcp.MessageEncoder;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * 模拟TCP传输实现
 * <p>
 * 用于测试Transport接口的功能，不进行实际的网络通信
 * </p>
 */
public class MockTcpTransport implements Transport {
    
    private TransportState state = TransportState.INITIALIZED;
    private TransportConfig config = TransportConfig.createDefault();
    private MessageHandler messageHandler;
    
    // 模拟发送和接收队列
    private final Queue<byte[]> sendQueue = new LinkedList<>();
    private final Queue<byte[]> receiveQueue = new LinkedList<>();
    
    @Override
    public void connect(String host, int port) throws TransportException {
        if (state != TransportState.INITIALIZED && state != TransportState.DISCONNECTED) {
            throw new IllegalStateException("连接状态错误: " + state);
        }
        
        state = TransportState.CONNECTING;
        // 模拟连接
        state = TransportState.CONNECTED;
    }
    
    @Override
    public CompletableFuture<Void> connectAsync(String host, int port) {
        return CompletableFuture.runAsync(() -> {
            try {
                connect(host, port);
            } catch (TransportException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    @Override
    public void disconnect() {
        if (state != TransportState.CONNECTED) {
            throw new IllegalStateException("未连接，无法断开: " + state);
        }
        
        state = TransportState.DISCONNECTING;
        // 模拟断开连接
        sendQueue.clear();
        receiveQueue.clear();
        state = TransportState.DISCONNECTED;
    }
    
    @Override
    public CompletableFuture<Void> disconnectAsync() {
        return CompletableFuture.runAsync(this::disconnect);
    }
    
    @Override
    public void send(byte[] data) throws TransportException {
        if (state != TransportState.CONNECTED) {
            throw TransportException.connectionClosed("模拟连接");
        }
        
        // 将原始数据编码后放入发送队列
        byte[] encodedData = MessageEncoder.encode(data);
        sendQueue.add(encodedData);
        
        // 模拟数据收到，将数据解码后放入接收队列
        if (messageHandler != null) {
            // 解码数据
            byte[] decodedData = MessageDecoder.decode(encodedData);
            // 处理消息
            messageHandler.onMessage(decodedData);
        }
    }
    
    @Override
    public CompletableFuture<Void> sendAsync(byte[] data) {
        return CompletableFuture.runAsync(() -> {
            try {
                send(data);
            } catch (TransportException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    @Override
    public byte[] receive() throws TransportException {
        if (state != TransportState.CONNECTED) {
            throw TransportException.connectionClosed("模拟连接");
        }
        
        // 从接收队列中取出数据
        byte[] encodedData = receiveQueue.poll();
        if (encodedData == null) {
            return null;
        }
        
        // 解码数据
        return MessageDecoder.decode(encodedData);
    }
    
    @Override
    public CompletableFuture<byte[]> receiveAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return receive();
            } catch (TransportException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    @Override
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }
    
    @Override
    public TransportState getState() {
        return state;
    }
    
    @Override
    public boolean isConnected() {
        return state == TransportState.CONNECTED;
    }
    
    @Override
    public TransportConfig getConfig() {
        return config;
    }
    
    @Override
    public void setConfig(TransportConfig config) {
        this.config = config;
    }
    
    /**
     * 模拟接收消息
     *
     * @param data 原始消息数据
     */
    public void mockReceive(byte[] data) {
        if (state != TransportState.CONNECTED) {
            throw new IllegalStateException("未连接，无法接收消息: " + state);
        }
        
        // 编码消息
        byte[] encodedData = MessageEncoder.encode(data);
        // 加入接收队列
        receiveQueue.add(encodedData);
        
        // 如果有消息处理器，立即处理
        if (messageHandler != null) {
            messageHandler.onMessage(data);
        }
    }
    
    /**
     * 获取发送队列大小
     *
     * @return 发送队列大小
     */
    public int getSendQueueSize() {
        return sendQueue.size();
    }
    
    /**
     * 获取接收队列大小
     *
     * @return 接收队列大小
     */
    public int getReceiveQueueSize() {
        return receiveQueue.size();
    }
} 