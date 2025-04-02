package plato.state.infrastructure.timer;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 时间轮算法实现
 * 用于高效管理大量定时任务，降低资源消耗
 * 对应Go版本中的timingwheel包
 */
@Slf4j
public class TimingWheel {

    /**
     * 时间槽数量
     */
    private final int ticksPerWheel;

    /**
     * 时间槽间隔（毫秒）
     */
    private final long tickDuration;

    /**
     * 时间槽
     */
    private final List<Slot> wheel;

    /**
     * 当前时间槽位置
     */
    private final AtomicInteger currentTickIndex = new AtomicInteger(0);

    /**
     * 是否运行中
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 调度器
     */
    private final ScheduledExecutorService scheduler;

    /**
     * 任务执行器
     */
    private final ExecutorService taskExecutor;

    /**
     * 构造函数
     *
     * @param tickDuration  时间槽间隔（毫秒）
     * @param ticksPerWheel 时间槽数量
     */
    public TimingWheel(long tickDuration, int ticksPerWheel) {
        this.tickDuration = tickDuration;
        this.ticksPerWheel = ticksPerWheel;
        this.wheel = new ArrayList<>(ticksPerWheel);
        
        // 初始化时间轮
        for (int i = 0; i < ticksPerWheel; i++) {
            wheel.add(new Slot());
        }
        
        // 创建调度器和执行器
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "timing-wheel-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        
        this.taskExecutor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                r -> {
                    Thread thread = new Thread(r, "timing-wheel-executor");
                    thread.setDaemon(true);
                    return thread;
                });
    }

    /**
     * 启动时间轮
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            // 启动时间轮调度
            scheduler.scheduleAtFixedRate(
                    this::tick,
                    tickDuration,
                    tickDuration,
                    TimeUnit.MILLISECONDS
            );
            log.info("TimingWheel started with tickDuration={}ms, ticksPerWheel={}", 
                    tickDuration, ticksPerWheel);
        }
    }

    /**
     * 停止时间轮
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            scheduler.shutdown();
            taskExecutor.shutdown();
            log.info("TimingWheel stopped");
        }
    }

    /**
     * 添加定时任务
     *
     * @param delay    延迟时间（毫秒）
     * @param runnable 任务
     * @return 定时器
     */
    public Timer newTimeout(long delay, Runnable runnable) {
        if (delay < 0) {
            throw new IllegalArgumentException("Delay must be >= 0");
        }
        
        // 计算需要经过的tick数
        long ticks = delay / tickDuration;
        // 计算目标槽位
        int stopIndex = (int) ((currentTickIndex.get() + ticks) % ticksPerWheel);
        
        // 创建定时器
        Timer timer = new Timer(runnable);
        // 将定时器添加到对应的槽位
        wheel.get(stopIndex).addTimer(timer);
        
        return timer;
    }

    /**
     * 创建延迟执行的任务
     *
     * @param delay    延迟时间（毫秒）
     * @param runnable 任务
     * @return 定时器
     */
    public Timer afterFunc(long delay, Runnable runnable) {
        return newTimeout(delay, runnable);
    }

    /**
     * 时间轮滴答，推进时间轮
     */
    private void tick() {
        if (!running.get()) {
            return;
        }
        
        try {
            // 获取当前槽位
            int currentIndex = currentTickIndex.getAndIncrement() % ticksPerWheel;
            Slot slot = wheel.get(currentIndex);
            
            // 执行当前槽位中的所有定时任务
            slot.expireTimers().forEach(timer -> {
                if (!timer.isCancelled()) {
                    taskExecutor.submit(() -> {
                        try {
                            timer.getTask().run();
                        } catch (Exception e) {
                            log.error("Error executing timer task", e);
                        }
                    });
                }
            });
        } catch (Exception e) {
            log.error("Error in timing wheel tick", e);
        }
    }

    /**
     * 时间槽
     */
    private static class Slot {
        private final Map<Timer, Timer> timers = new ConcurrentHashMap<>();

        /**
         * 添加定时器
         *
         * @param timer 定时器
         */
        public void addTimer(Timer timer) {
            timers.put(timer, timer);
        }

        /**
         * 移除定时器
         *
         * @param timer 定时器
         */
        public void removeTimer(Timer timer) {
            timers.remove(timer);
        }

        /**
         * 获取并清空当前槽位中的所有定时器
         *
         * @return 定时器列表
         */
        public List<Timer> expireTimers() {
            if (timers.isEmpty()) {
                return List.of();
            }
            
            List<Timer> expired = new ArrayList<>(timers.values());
            timers.clear();
            return expired;
        }
    }

    /**
     * 定时器
     */
    public class Timer {
        private final Runnable task;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        /**
         * 构造函数
         *
         * @param task 任务
         */
        public Timer(Runnable task) {
            this.task = task;
        }

        /**
         * 获取任务
         *
         * @return 任务
         */
        public Runnable getTask() {
            return task;
        }

        /**
         * 取消定时器
         */
        public void stop() {
            cancelled.set(true);
        }

        /**
         * 是否已取消
         *
         * @return 是否已取消
         */
        public boolean isCancelled() {
            return cancelled.get();
        }
    }
} 