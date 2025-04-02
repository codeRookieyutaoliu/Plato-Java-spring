package plato.state.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis分片配置
 * 配置Redis分片相关参数
 * 对应Go版本中的Redis分片配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "state.redis.shard")
public class RedisShardConfig {

    /**
     * 是否启用分片
     */
    private boolean enabled = true;

    /**
     * 分片数量
     */
    private int shardCount = 1024;

    /**
     * 当前实例负责的分片范围
     * 格式：起始分片ID-结束分片ID，例如：0-255
     */
    private String shardRange = "0-1023";

    /**
     * 连接状态键前缀
     */
    private String connStateKeyPrefix = "conn_state:";

    /**
     * 客户端ID键前缀
     */
    private String clientIdKeyPrefix = "client_id:";

    /**
     * 最后一条消息键前缀
     */
    private String lastMsgKeyPrefix = "last_msg:";

    /**
     * 消息定时器锁键前缀
     */
    private String msgTimerLockKeyPrefix = "msg_timer_lock:";

    /**
     * 获取当前实例负责的分片ID列表
     *
     * @return 分片ID列表
     */
    public List<Integer> getShardIds() {
        List<Integer> shardIds = new ArrayList<>();
        
        if (shardRange == null || shardRange.isEmpty()) {
            return shardIds;
        }
        
        String[] parts = shardRange.split("-");
        if (parts.length != 2) {
            return shardIds;
        }
        
        try {
            int startShardId = Integer.parseInt(parts[0]);
            int endShardId = Integer.parseInt(parts[1]);
            
            for (int i = startShardId; i <= endShardId; i++) {
                shardIds.add(i);
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        
        return shardIds;
    }

    /**
     * 判断指定的分片ID是否由当前实例负责
     *
     * @param shardId 分片ID
     * @return 是否由当前实例负责
     */
    public boolean isResponsibleForShard(int shardId) {
        List<Integer> shardIds = getShardIds();
        return shardIds.contains(shardId);
    }

    /**
     * 计算连接ID对应的分片ID
     *
     * @param connId 连接ID
     * @return 分片ID
     */
    public int getShardId(long connId) {
        return (int) (connId % shardCount);
    }

    /**
     * 获取连接状态键
     *
     * @param connId 连接ID
     * @return 连接状态键
     */
    public String getConnStateKey(long connId) {
        int shardId = getShardId(connId);
        return connStateKeyPrefix + shardId + ":" + connId;
    }

    /**
     * 获取客户端ID键
     *
     * @param connId 连接ID
     * @param sessionId 会话ID
     * @return 客户端ID键
     */
    public String getClientIdKey(long connId, String sessionId) {
        int shardId = getShardId(connId);
        return clientIdKeyPrefix + shardId + ":" + connId + ":" + sessionId;
    }

    /**
     * 获取最后一条消息键
     *
     * @param connId 连接ID
     * @return 最后一条消息键
     */
    public String getLastMsgKey(long connId) {
        int shardId = getShardId(connId);
        return lastMsgKeyPrefix + shardId + ":" + connId;
    }

    /**
     * 获取消息定时器锁键
     *
     * @param connId 连接ID
     * @return 消息定时器锁键
     */
    public String getMsgTimerLockKey(long connId) {
        int shardId = getShardId(connId);
        return msgTimerLockKeyPrefix + shardId + ":" + connId;
    }
} 