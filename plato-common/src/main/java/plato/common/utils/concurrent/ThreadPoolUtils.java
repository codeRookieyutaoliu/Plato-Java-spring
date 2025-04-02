package plato.common.utils.concurrent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工具类
 * <p>
 * 提供线程池创建和管理的工具方法，包括：
 * - 创建不同类型的线程池
 * - 线程池参数优化
 * - 线程工厂
 * </p>
 * 在Go代码中通常使用goroutine池实现类似功能
 */
public class ThreadPoolUtils {
    /**
     * 私有构造函数，防止实例化
     */
    private ThreadPoolUtils() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * CPU核心数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 默认核心线程数 - CPU核心数+1
     */
    private static final int DEFAULT_CORE_POOL_SIZE = CPU_COUNT + 1;

    /**
     * 默认最大线程数 - CPU核心数*2+1
     */
    private static final int DEFAULT_MAX_POOL_SIZE = CPU_COUNT * 2 + 1;

    /**
     * 默认线程存活时间（秒）
     */
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;

    /**
     * 默认队列容量
     */
    private static final int DEFAULT_QUEUE_CAPACITY = 1000;

    /**
     * 线程工厂计数器
     */
    private static final AtomicInteger POOL_COUNTER = new AtomicInteger(1);

    /**
     * 创建默认线程池
     *
     * @param poolName 线程池名称
     * @return 线程池执行器
     */
    public static ThreadPoolExecutor createDefaultThreadPool(String poolName) {
        return createThreadPool(
                poolName,
                DEFAULT_CORE_POOL_SIZE,
                DEFAULT_MAX_POOL_SIZE,
                DEFAULT_KEEP_ALIVE_SECONDS,
                DEFAULT_QUEUE_CAPACITY
        );
    }

    /**
     * 创建IO密集型线程池
     * 适合IO密集型任务，线程数较多
     *
     * @param poolName 线程池名称
     * @return 线程池执行器
     */
    public static ThreadPoolExecutor createIoIntensiveThreadPool(String poolName) {
        return createThreadPool(
                poolName,
                CPU_COUNT * 2,
                CPU_COUNT * 4,
                DEFAULT_KEEP_ALIVE_SECONDS,
                DEFAULT_QUEUE_CAPACITY
        );
    }

    /**
     * 创建CPU密集型线程池
     * 适合CPU密集型任务，线程数接近CPU核心数
     *
     * @param poolName 线程池名称
     * @return 线程池执行器
     */
    public static ThreadPoolExecutor createCpuIntensiveThreadPool(String poolName) {
        return createThreadPool(
                poolName,
                CPU_COUNT,
                CPU_COUNT + 1,
                DEFAULT_KEEP_ALIVE_SECONDS,
                DEFAULT_QUEUE_CAPACITY
        );
    }

    /**
     * 创建自定义线程池
     *
     * @param poolName 线程池名称
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param keepAliveSeconds 线程存活时间（秒）
     * @param queueCapacity 队列容量
     * @return 线程池执行器
     */
    public static ThreadPoolExecutor createThreadPool(
            String poolName,
            int corePoolSize,
            int maxPoolSize,
            int keepAliveSeconds,
            int queueCapacity
    ) {
        // 使用有界队列
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);
        
        // 创建线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadCounter = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(poolName + "-" + threadCounter.getAndIncrement());
                thread.setDaemon(true); // 设置为守护线程
                return thread;
            }
        };
        
        // 创建拒绝策略处理器
        RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                rejectedExecutionHandler
        );
        
        // 预启动所有核心线程
        executor.prestartAllCoreThreads();
        
        return executor;
    }

    /**
     * 创建单线程执行器
     *
     * @param poolName 线程池名称
     * @return 单线程执行器
     */
    public static ExecutorService createSingleThreadExecutor(String poolName) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName(poolName + "-" + POOL_COUNTER.getAndIncrement());
            thread.setDaemon(true); // 设置为守护线程
            return thread;
        });
    }

    /**
     * 创建计划任务执行器
     *
     * @param poolSize 线程池大小
     * @param poolName 线程池名称
     * @return 计划任务执行器
     */
    public static ScheduledExecutorService createScheduledThreadPool(int poolSize, String poolName) {
        return Executors.newScheduledThreadPool(poolSize, r -> {
            Thread thread = new Thread(r);
            thread.setName(poolName + "-scheduled-" + POOL_COUNTER.getAndIncrement());
            thread.setDaemon(true); // 设置为守护线程
            return thread;
        });
    }

    /**
     * 优雅关闭线程池
     *
     * @param executor 线程池执行器
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     */
    public static void shutdown(ExecutorService executor, long timeout, TimeUnit timeUnit) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(timeout, timeUnit)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
} 