package plato.state.domain.service;

import plato.state.domain.model.UserState;

/**
 * 用户状态领域服务接口
 * 处理用户状态相关的核心业务逻辑
 */
public interface UserStateDomainService {
    
    /**
     * 用户登录
     *
     * @param userId      用户ID
     * @param connectionId 连接ID
     * @param endpoint    终端地址
     * @return 是否登录成功
     */
    boolean login(Long userId, Long connectionId, String endpoint);
    
    /**
     * 用户登出
     *
     * @param connectionId 连接ID
     * @return 是否登出成功
     */
    boolean logout(Long connectionId);
    
    /**
     * 发送消息
     *
     * @param fromUserId 发送者ID
     * @param toUserId   接收者ID
     * @param payload    消息内容
     * @return 是否发送成功
     */
    boolean sendMessage(Long fromUserId, Long toUserId, byte[] payload);
    
    /**
     * 获取用户状态
     *
     * @param userId 用户ID
     * @return 用户状态
     */
    UserState getUserState(Long userId);
    
    /**
     * 获取连接对应的用户ID
     *
     * @param connectionId 连接ID
     * @return 用户ID
     */
    Long getUserIdByConnectionId(Long connectionId);
} 