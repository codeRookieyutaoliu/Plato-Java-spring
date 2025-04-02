package plato.common.bus;

import java.util.function.Consumer;

/**
 * 事件总线接口
 * 定义事件发布和订阅的基本操作
 */
public interface EventBus {
    /**
     * 发布事件
     * @param topic 主题
     * @param event 事件对象
     */
    void publish(String topic, Object event);

    /**
     * 订阅事件
     * @param topic 主题
     * @param handler 事件处理器
     * @return 订阅ID
     */
    String subscribe(String topic, Consumer<Object> handler);

    /**
     * 取消订阅
     * @param subscriptionId 订阅ID
     * @return 是否取消成功
     */
    boolean unsubscribe(String subscriptionId);

    /**
     * 获取主题的订阅者数量
     * @param topic 主题
     * @return 订阅者数量
     */
    int getSubscriberCount(String topic);

    /**
     * 清空指定主题的所有订阅
     * @param topic 主题
     */
    void clearTopic(String topic);

    /**
     * 获取事件总线状态
     * @return 是否正在运行
     */
    boolean isRunning();
}