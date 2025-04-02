package plato.common.timingwheel;

import java.util.concurrent.TimeUnit;

/**
 * 时间轮接口
 * 定义时间轮的基本操作，用于高效处理延时任务
 */
public interface TimingWheel {
    /**
     * 添加延时任务
     * @param task 任务实现
     * @param delay 延时时间
     * @param unit 时间单位
     * @return 任务ID
     */
    String addTask(TimingTask task, long delay, TimeUnit unit);

    /**
     * 取消任务
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancelTask(String taskId);

    /**
     * 启动时间轮
     */
    void start();

    /**
     * 停止时间轮
     */
    void stop();

    /**
     * 获取时间轮状态
     * @return 是否正在运行
     */
    boolean isRunning();

    /**
     * 获取待执行的任务数量
     * @return 任务数量
     */
    int getPendingTaskCount();
}