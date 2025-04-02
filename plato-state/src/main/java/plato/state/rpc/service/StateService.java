package plato.state.rpc.service;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import plato.state.application.CommandProcessor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 状态服务
 * 实现State服务的gRPC接口
 * 对应Go版本中的state/rpc/service/service.go文件中的Service结构体
 */
@Slf4j
@Service
public class StateService extends StateGrpc.StateImplBase {

    /**
     * 取消连接命令
     * 对应Go版本中的CancelConnCmd常量
     */
    public static final int CANCEL_CONN_CMD = 1;

    /**
     * 发送消息命令
     * 对应Go版本中的SendMsgCmd常量
     */
    public static final int SEND_MSG_CMD = 2;

    /**
     * 命令通道容量
     * 对应Go版本中的config.GetSateCmdChannelNum()
     */
    @Value("${state.cmd.channel.capacity:1000}")
    private int cmdChannelCapacity;

    /**
     * 工作线程池大小
     */
    @Value("${state.work.pool.size:100}")
    private int workPoolSize;

    /**
     * 命令通道
     * 对应Go版本中的CmdChannel字段
     */
    private final BlockingQueue<CommandContext> cmdChannel;

    /**
     * 命令处理器
     * 用于处理命令
     */
    private final CommandProcessor commandProcessor;

    /**
     * 工作线程池
     * 用于异步处理命令
     */
    private ThreadPoolExecutor workPool;

    /**
     * 命令处理线程
     */
    private Thread cmdHandlerThread;

    /**
     * 是否运行中
     */
    private volatile boolean running = true;

    /**
     * 构造函数
     *
     * @param commandProcessor 命令处理器
     */
    public StateService(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
        this.cmdChannel = new LinkedBlockingQueue<>(cmdChannelCapacity);
    }

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        // 初始化工作线程池
        workPool = new ThreadPoolExecutor(
                workPoolSize,
                workPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("state-work-" + t.getId());
                    return t;
                }
        );
        
        // 启动命令处理线程
        startCommandProcessor();
        
        log.info("StateService initialized with command channel capacity: {}", cmdChannelCapacity);
    }

    /**
     * 启动命令处理线程
     * 对应Go版本中的cmdHandler函数
     */
    private void startCommandProcessor() {
        cmdHandlerThread = new Thread(() -> {
            log.info("Command processor thread started");
            try {
                while (running) {
                    try {
                        // 从命令通道获取命令
                        CommandContext cmdCtx = cmdChannel.poll(100, TimeUnit.MILLISECONDS);
                        if (cmdCtx != null) {
                            // 提交到工作线程池处理
                            workPool.execute(() -> commandProcessor.process(cmdCtx));
                        }
                    } catch (InterruptedException e) {
                        if (running) {
                            log.error("Command processor thread interrupted", e);
                        }
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Error processing command", e);
                    }
                }
            } finally {
                log.info("Command processor thread stopped");
            }
        }, "state-cmd-processor");
        
        cmdHandlerThread.setDaemon(true);
        cmdHandlerThread.start();
    }

    /**
     * 取消连接
     * 对应Go版本中的CancelConn方法
     *
     * @param request          请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void cancelConn(StateRequest request, StreamObserver<StateResponse> responseObserver) {
        try {
            // 创建命令上下文
            CommandContext cmdContext = new CommandContext();
            cmdContext.setCmd(CommandContext.CANCEL_CONN_CMD);
            cmdContext.setEndpoint(request.getEndpoint());
            cmdContext.setConnId(request.getConnId());
            
            if (request.getData() != null && !request.getData().isEmpty()) {
                cmdContext.setPayload(request.getData().toByteArray());
            }
            
            // 提交到命令通道
            boolean success = cmdChannel.offer(cmdContext);
            
            if (success) {
                log.debug("Received cancelConn request: endpoint={}, connId={}", 
                        request.getEndpoint(), request.getConnId());
                
                // 返回成功响应
                StateResponse response = StateResponse.newBuilder()
                        .setCode(0)
                        .setMsg("success")
                        .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                log.warn("Command channel full, failed to process cancelConn request: endpoint={}, connId={}", 
                        request.getEndpoint(), request.getConnId());
                
                // 返回错误响应
                StateResponse response = StateResponse.newBuilder()
                        .setCode(503)
                        .setMsg("command channel full")
                        .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            log.error("Error processing cancelConn request: endpoint={}, connId={}", 
                    request.getEndpoint(), request.getConnId(), e);
            
            // 返回错误响应
            StateResponse response = StateResponse.newBuilder()
                    .setCode(500)
                    .setMsg("error: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * 发送消息
     * 对应Go版本中的SendMsg方法
     *
     * @param request          请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void sendMsg(StateRequest request, StreamObserver<StateResponse> responseObserver) {
        try {
            // 创建命令上下文
            CommandContext cmdContext = new CommandContext();
            cmdContext.setCmd(CommandContext.SEND_MSG_CMD);
            cmdContext.setEndpoint(request.getEndpoint());
            cmdContext.setConnId(request.getConnId());
            cmdContext.setPayload(request.getData().toByteArray());
            
            // 提交到命令通道
            boolean success = cmdChannel.offer(cmdContext);
            
            if (success) {
                log.debug("Received sendMsg request: endpoint={}, connId={}, dataSize={}", 
                        request.getEndpoint(), request.getConnId(), request.getData().size());
                
                // 返回成功响应
                StateResponse response = StateResponse.newBuilder()
                        .setCode(0)
                        .setMsg("success")
                        .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                log.warn("Command channel full, failed to process sendMsg request: endpoint={}, connId={}", 
                        request.getEndpoint(), request.getConnId());
                
                // 返回错误响应
                StateResponse response = StateResponse.newBuilder()
                        .setCode(503)
                        .setMsg("command channel full")
                        .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            log.error("Error processing sendMsg request: endpoint={}, connId={}", 
                    request.getEndpoint(), request.getConnId(), e);
            
            // 返回错误响应
            StateResponse response = StateResponse.newBuilder()
                    .setCode(500)
                    .setMsg("error: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        running = false;
        
        if (cmdHandlerThread != null) {
            cmdHandlerThread.interrupt();
            try {
                cmdHandlerThread.join(5000);
            } catch (InterruptedException e) {
                log.error("Error waiting for command processor thread to stop", e);
                Thread.currentThread().interrupt();
            }
        }
        
        if (workPool != null) {
            workPool.shutdown();
            try {
                if (!workPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    workPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Error shutting down work pool", e);
                workPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("StateService shutdown");
    }
} 