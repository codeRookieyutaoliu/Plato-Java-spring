package plato.state.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 连接信息
 * 表示用户的一个连接
 * 对应Go中的连接信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionInfo {
    /**
     * 连接ID
     * 对应Go中的连接ID
     */
    private Long connId;
    
    /**
     * 终端地址
     * 对应Go中的终端地址
     */
    private String endpoint;
    
    /**
     * 连接时间
     * 对应Go中的连接时间
     */
    private LocalDateTime connectedTime;
    
    /**
     * 最后活跃时间
     * 对应Go中的最后活跃时间
     */
    private LocalDateTime lastActiveTime;
    
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
     * 判断连接是否活跃
     * 对应Go中的活跃状态判断
     *
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否活跃
     */
    public boolean isActive(int timeoutSeconds) {
        // 判断连接是否活跃
        // 对应Go中的活跃状态判断逻辑
        return LocalDateTime.now().minusSeconds(timeoutSeconds).isBefore(lastActiveTime);
    }
} 