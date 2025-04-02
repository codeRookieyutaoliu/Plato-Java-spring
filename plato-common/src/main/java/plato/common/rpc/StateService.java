package plato.common.rpc;

/**
 * 状态服务接口定义
 */
public interface StateService {
    /**
     * 发送消息到状态服务
     *
     * @param endpoint     服务端点
     * @param connectionId 连接ID
     * @param messageBytes 消息字节数组
     * @return 是否发送成功
     */
    boolean sendMessage(String endpoint, Long connectionId, byte[] messageBytes);

    /**
     * 通知状态服务连接关闭
     *
     * @param endpoint     服务端点
     * @param connectionId 连接ID
     * @return 是否通知成功
     */
    boolean cancelConnection(String endpoint, Long connectionId);
} 