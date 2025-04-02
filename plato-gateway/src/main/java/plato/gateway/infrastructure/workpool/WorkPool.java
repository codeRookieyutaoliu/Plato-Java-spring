package plato.gateway.infrastructure.workpool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * 工作池
 * 对应Go版本的workpool.go
 */
@Slf4j
@Component
public class WorkPool {
    
    // 线程池，对应Go版本的pool
    private ThreadPoolExecutor executor;
    
    // 工作队列，对应Go版本的queue
    private BlockingQueue<Runnable> workQueue;
    
    // 线程工厂，对应Go版本的factory
    private ThreadFactory threadFactory;
    
    // 核心线程数，对应Go版本的corePoolSize
    @Value("${plato.gateway.workpool.core-pool-size:10}")
    private int corePoolSize;
    
    // 最大线程数，对应Go版本的maxPoolSize
    @Value("${plato.gateway.workpool.max-pool-size:100}")
    private int maxPoolSize;
    
    // 线程空闲时间（秒），对应Go版本的keepAliveTime
    @Value("${plato.gateway.workpool.keep-alive-time:60}")
    private int keepAliveTime;
    
    // 队列容量，对应Go版本的queueCapacity
    @Value("${plato.gateway.workpool.queue-capacity:1000}")
    private int queueCapacity;
    
    /**
     * 初始化工作池
     * 对应Go版本的initWorkPoll方法
     */
    @PostConstruct
    public void init() {
        log.info("初始化工作池: corePoolSize={}, maxPoolSize={}, keepAliveTime={}s, queueCapacity={}",
                corePoolSize, maxPoolSize, keepAliveTime, queueCapacity);
        
        // 创建线程工厂
        threadFactory = new ThreadFactory() {
            private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = defaultFactory.newThread(r);
                thread.setName("plato-gateway-worker-" + thread.getId());
                return thread;
            }
        };
        
        // 创建工作队列
        workQueue = new LinkedBlockingQueue<>(queueCapacity);
        
        // 创建线程池
        executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);
        
        log.info("工作池初始化完成");
    }
    
    /**
     * 提交任务
     * 对应Go版本的Submit方法
     *
     * @param task 任务
     * @return 任务的Future对象
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }
    
    /**
     * 提交任务并返回结果
     * 对应Go版本的SubmitWithResult方法
     *
     * @param task 任务
     * @param <T>  结果类型
     * @return 任务的Future对象
     */
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }
    
    /**
     * 执行任务
     * 对应Go版本的Execute方法
     *
     * @param task 任务
     */
    public void execute(Runnable task) {
        executor.execute(task);
    }
    
    /**
     * 获取活跃线程数
     * 对应Go版本的GetActiveCount方法
     *
     * @return 活跃线程数
     */
    public int getActiveCount() {
        return executor.getActiveCount();
    }
    
    /**
     * 获取队列大小
     * 对应Go版本的GetQueueSize方法
     *
     * @return 队列大小
     */
    public int getQueueSize() {
        return workQueue.size();
    }
    
    /**
     * 获取已完成任务数
     * 对应Go版本的GetCompletedTaskCount方法
     *
     * @return 已完成任务数
     */
    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }
    
    /**
     * 关闭工作池
     * 对应Go版本的Close方法
     */
    @PreDestroy
    public void shutdown() {
        log.info("关闭工作池");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.error("工作池未能完全关闭");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("工作池已关闭");
    }
} 