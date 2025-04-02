package plato.state.infrastructure.timer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * 定时器管理器
 * 管理全局时间轮实例
 * 对应Go版本中的timer.go文件
 */
@Slf4j
@Component
public class TimerManager {

    /**
     * 全局时间轮实例
     */
    private TimingWheel timingWheel;

    /**
     * 初始化时间轮
     * 对应Go版本中的InitTimer函数
     */
    @PostConstruct
    public void init() {
        // 创建时间轮，时间槽间隔为1毫秒，槽数为20
        timingWheel = new TimingWheel(1, 20);
        timingWheel.start();
        log.info("TimerManager initialized");
    }

    /**
     * 关闭时间轮
     * 对应Go版本中的CloseTimer函数
     */
    @PreDestroy
    public void close() {
        if (timingWheel != null) {
            timingWheel.stop();
            log.info("TimerManager closed");
        }
    }

    /**
     * 创建延迟执行的任务
     * 对应Go版本中的AfterFunc函数
     *
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     * @param task     任务
     * @return 定时器
     */
    public TimingWheel.Timer afterFunc(long delay, TimeUnit timeUnit, Runnable task) {
        if (timingWheel == null) {
            throw new IllegalStateException("TimingWheel not initialized");
        }
        return timingWheel.afterFunc(timeUnit.toMillis(delay), task);
    }

    /**
     * 创建延迟执行的任务（毫秒）
     * 对应Go版本中的AfterFunc函数
     *
     * @param delayMillis 延迟时间（毫秒）
     * @param task        任务
     * @return 定时器
     */
    public TimingWheel.Timer afterFunc(long delayMillis, Runnable task) {
        return afterFunc(delayMillis, TimeUnit.MILLISECONDS, task);
    }
} 