package plato.state.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户状态实体
 * 表示用户的在线状态和连接信息
 * 对应Go中的用户状态实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserState {
    /**
     * 用户ID
     * 对应Go中的用户ID
     */
    private Long userId;
    
    /**
     * 用户连接列表
     * 对应Go中的用户连接列表
     */
    private List<ConnectionInfo> connections = new ArrayList<>();
    
    /**
     * 最后活跃时间
     * 对应Go中的最后活跃时间
     */
    private LocalDateTime lastActiveTime;
    
    /**
     * 用户状态
     * 对应Go中的用户状态
     */
    private UserStatus status;
    
    /**
     * 添加连接
     * 对应Go中的添加连接方法
     *
     * @param connectionInfo 连接信息
     */
    public void addConnection(ConnectionInfo connectionInfo) {
        // 检查连接是否已存在
        // 对应Go中的连接检查逻辑
        boolean exists = connections.stream()
                .anyMatch(conn -> conn.getConnId().equals(connectionInfo.getConnId()));
        
        if (!exists) {
            connections.add(connectionInfo);
        }
        
        // 更新最后活跃时间
        // 对应Go中的更新活跃时间逻辑
        updateLastActiveTime();
        
        // 更新用户状态
        // 对应Go中的更新用户状态逻辑
        if (status == UserStatus.OFFLINE) {
            status = UserStatus.ONLINE;
        }
    }
    
    /**
     * 移除连接
     * 对应Go中的移除连接方法
     *
     * @param connId 连接ID
     * @return 是否移除成功
     */
    public boolean removeConnection(Long connId) {
        // 移除连接
        // 对应Go中的移除连接逻辑
        boolean removed = connections.removeIf(conn -> conn.getConnId().equals(connId));
        
        // 如果没有连接，则将用户状态设置为离线
        // 对应Go中的状态更新逻辑
        if (connections.isEmpty()) {
            status = UserStatus.OFFLINE;
        }
        
        return removed;
    }
    
    /**
     * 更新最后活跃时间
     * 对应Go中的更新活跃时间方法
     */
    public void updateLastActiveTime() {
        // 更新最后活跃时间
        // 对应Go中的更新活跃时间逻辑
        this.lastActiveTime = LocalDateTime.now();
    }
    
    /**
     * 判断用户是否在线
     * 对应Go中的在线状态判断
     *
     * @return 是否在线
     */
    public boolean isOnline() {
        return status == UserStatus.ONLINE && !connections.isEmpty();
    }
    
    /**
     * 连接信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionInfo {
        /**
         * 连接ID
         */
        private Long connId;
        
        /**
         * 终端地址
         */
        private String endpoint;
        
        /**
         * 连接时间
         */
        private LocalDateTime connectedTime;
    }
    
    /**
     * 用户状态枚举
     * 对应Go中的用户状态枚举
     */
    public enum UserStatus {
        /**
         * 在线
         * 对应Go中的在线状态
         */
        ONLINE,
        
        /**
         * 离线
         * 对应Go中的离线状态
         */
        OFFLINE
    }
} 