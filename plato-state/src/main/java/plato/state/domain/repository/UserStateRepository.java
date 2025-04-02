package plato.state.domain.repository;

import plato.state.domain.model.UserState;

import java.util.List;

/**
 * 用户状态仓储接口
 * 定义用户状态数据访问的抽象
 */
public interface UserStateRepository {
    
    /**
     * 保存用户状态
     *
     * @param userState 用户状态
     */
    void save(UserState userState);
    
    /**
     * 根据用户ID查找用户状态
     *
     * @param userId 用户ID
     * @return 用户状态
     */
    UserState findByUserId(Long userId);
    
    /**
     * 根据连接ID查找用户ID
     *
     * @param connectionId 连接ID
     * @return 用户ID
     */
    Long findUserIdByConnectionId(Long connectionId);
    
    /**
     * 删除用户状态
     *
     * @param userId 用户ID
     */
    void delete(Long userId);
    
    /**
     * 获取所有在线用户
     *
     * @return 在线用户列表
     */
    List<UserState> findOnlineUsers();
} 