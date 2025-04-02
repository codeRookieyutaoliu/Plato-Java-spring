package plato.state.application.service;

/**
 * 状态应用服务接口
 * 处理用户状态相关的应用层操作
 */
public interface StateService {

    /**
     * 用户登录
     *
     * @param userId   用户ID
     * @param connId   连接ID
     * @param endpoint 终端地址
     * @return 是否登录成功
     */
    boolean login(Long userId, Long connId, String endpoint);

    /**
     * 用户登出
     *
     * @param connId 连接ID
     * @return 是否登出成功
     */
    boolean logout(Long connId);

    /**
     * 发送消息
     *
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @param payload    消息内容
     * @return 是否发送成功
     */
    boolean sendMessage(Long fromUserId, Long toUserId, byte[] payload);
} 