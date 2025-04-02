package plato.state.domain.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import plato.common.id.SnowflakeIdGenerator;
import plato.common.router.RouterTable;
import plato.state.config.RedisShardConfig;
import plato.state.infrastructure.timer.TimerManager;
import plato.state.rpc.GatewayClient;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存状态管理器
 * 负责管理连接状态和消息缓存
 * 对应Go版本中的cacheStateManager结构体及相关函数
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheStateManager {

    /**
     * 消息ID
     * 注意：此字段已被MessageStateMachine替代，保留是为了兼容性
     */
    @Deprecated
    public long msgId = 0;

    /**
     * 连接状态映射
     * 本地缓存，用于快速访问
     * 对应Go版本中的connStateMap
     */
    private final Map<Long, ConnState> connStateMap = new ConcurrentHashMap<>();

    /**
     * 定时器管理器
     */
    private final TimerManager timerManager;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 网关客户端
     */
    private final GatewayClient gatewayClient;
    
    /**
     * 路由表
     */
    private final RouterTable routerTable;
    
    /**
     * Redis分片配置
     */
    private final RedisShardConfig redisShardConfig;
    
    /**
     * 雪花算法ID生成器
     */
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 登录槽范围
     * 对应Go版本中的loginSlotRange配置
     */
    @Value("${state.login-slot-range:0,1,2,3,4,5,6,7}")
    private List<Integer> loginSlotRange;

    /**
     * 服务地址
     * 对应Go版本中的serviceAddress配置
     */
    @Value("${state.service.address:127.0.0.1}")
    private String serviceAddress;

    /**
     * 服务端口
     * 对应Go版本中的servicePort配置
     */
    @Value("${state.service.port:8902}")
    private int servicePort;

    /**
     * 比较并递增客户端ID的Lua脚本
     * 注意：此字段已被MessageStateMachine替代，保留是为了兼容性
     */
    @Deprecated
    private static final String COMPARE_AND_INCR_CLIENT_ID_SCRIPT = 
            "local current = redis.call('get', KEYS[1]); " +
            "if (current == false) then " +
            "  redis.call('set', KEYS[1], ARGV[1]); " +
            "  return 1; " +
            "end; " +
            "if (tonumber(ARGV[1]) <= tonumber(current)) then " +
            "  return 0; " +
            "end; " +
            "redis.call('set', KEYS[1], ARGV[1]); " +
            "return 1;";

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        // 初始化登录槽
        initLoginSlot();
        
        // 加载负责的分片中的连接状态
        loadShardConnStates();
        
        log.info("CacheStateManager initialized with shardRange: {}", redisShardConfig.getShardRange());
    }

    /**
     * 初始化登录槽
     * 对应Go版本中的initLoginSlot函数
     */
    private void initLoginSlot() {
        try {
            // 获取本地IP地址
            String localIp = serviceAddress;
            
            // 构建端点
            String endpoint = localIp + ":" + servicePort;
            
            // 遍历登录槽范围
            for (Integer slot : loginSlotRange) {
                // 构建登录槽键
                String key = "login_slot:" + slot;
                
                // 设置登录槽
                redisTemplate.opsForValue().set(key, endpoint);
                
                log.info("Initialized login slot: slot={}, endpoint={}", slot, endpoint);
            }
        } catch (Exception e) {
            log.error("Error initializing login slots", e);
        }
    }
    
    /**
     * 加载负责的分片中的连接状态
     * 在分布式环境中，每个实例只负责一部分分片
     */
    private void loadShardConnStates() {
        try {
            List<Integer> shardIds = redisShardConfig.getShardIds();
            log.info("Loading connection states for shards: {}", shardIds);
            
            for (Integer shardId : shardIds) {
                // 获取分片中的所有连接状态键
                String pattern = redisShardConfig.getConnStateKeyPrefix() + shardId + ":*";
                
                // 这里需要使用Redis的SCAN命令，但Spring Data Redis没有直接提供API
                // 在实际实现中，可以使用RedisTemplate的execute方法执行SCAN命令
                // 或者使用Jedis/Lettuce等底层客户端的API
                
                // 为了简化示例，这里只记录日志
                log.info("Would scan for connection states with pattern: {}", pattern);
            }
        } catch (Exception e) {
            log.error("Error loading shard connection states", e);
        }
    }

    /**
     * 创建新的连接状态
     * 对应Go版本中的newConnState函数
     *
     * @param deviceId 设备ID
     * @param connId   连接ID
     * @return 连接状态
     */
    public ConnState newConnState(long deviceId, long connId) {
        // 创建连接状态
        ConnState state = new ConnState(connId, deviceId, timerManager, redisTemplate, this);
        
        // 存储连接状态到本地缓存
        connStateMap.put(connId, state);
        
        // 存储连接状态到Redis
        storeConnIdState(connId, state);
        
        log.info("Created new connection state: deviceId={}, connId={}", deviceId, connId);
        
        return state;
    }

    /**
     * 连接登录
     * 对应Go版本中的connLogin函数
     *
     * @param deviceId 设备ID
     * @param connId   连接ID
     * @return 是否成功
     */
    public boolean connLogin(long deviceId, long connId) {
        try {
            // 检查设备ID和连接ID是否有效
            if (deviceId <= 0 || connId <= 0) {
                log.warn("Invalid device ID or connection ID: deviceId={}, connId={}", deviceId, connId);
                return false;
            }
            
            // 创建新的连接状态
            ConnState state = newConnState(deviceId, connId);
            
            // 添加路由记录
            String endpoint = serviceAddress + ":" + servicePort;
            boolean success = routerTable.addRecord(deviceId, endpoint, connId);
            
            if (!success) {
                log.warn("Failed to add route record: deviceId={}, connId={}", deviceId, connId);
                return false;
            }
            
            log.info("Connection login successful: deviceId={}, connId={}", deviceId, connId);
            return true;
        } catch (Exception e) {
            log.error("Error during connection login: deviceId={}, connId={}", deviceId, connId, e);
            return false;
        }
    }

    /**
     * 连接重新登录
     * 对应Go版本中的connReLogin函数
     *
     * @param deviceId 设备ID
     * @param connId   连接ID
     */
    public void connReLogin(long deviceId, long connId) {
        try {
            // 检查设备ID和连接ID是否有效
            if (deviceId <= 0 || connId <= 0) {
                log.warn("Invalid device ID or connection ID for re-login: deviceId={}, connId={}", deviceId, connId);
                return;
            }
            
            // 创建新的连接状态
            ConnState state = newConnState(deviceId, connId);
            
            // 添加路由记录
            String endpoint = serviceAddress + ":" + servicePort;
            boolean success = routerTable.addRecord(deviceId, endpoint, connId);
            
            if (!success) {
                log.warn("Failed to add route record during re-login: deviceId={}, connId={}", deviceId, connId);
                return;
            }
            
            log.info("Connection re-login successful: deviceId={}, connId={}", deviceId, connId);
        } catch (Exception e) {
            log.error("Error during connection re-login: deviceId={}, connId={}", deviceId, connId, e);
        }
    }

    /**
     * 连接登出
     * 对应Go版本中的connLogOut函数
     *
     * @param connId 连接ID
     * @return 设备ID
     */
    public long connLogOut(long connId) {
        try {
            // 获取连接状态
            ConnState state = loadConnIdState(connId);
            
            if (state == null) {
                log.warn("Connection state not found for logout: connId={}", connId);
                return 0;
            }
            
            // 获取设备ID
            long deviceId = state.getDeviceId();
            
            // 关闭连接状态
            state.close();
            
            // 移除连接状态
            deleteConnIdState(connId);
            
            log.info("Connection logout successful: deviceId={}, connId={}", deviceId, connId);
            
            return deviceId;
        } catch (Exception e) {
            log.error("Error during connection logout: connId={}", connId, e);
            return 0;
        }
    }

    /**
     * 重新连接
     * 对应Go版本中的reConn函数
     *
     * @param oldConnId 旧连接ID
     * @param newConnId 新连接ID
     * @return 是否成功
     */
    public boolean reConn(long oldConnId, long newConnId) {
        try {
            // 获取旧连接状态
            ConnState oldState = loadConnIdState(oldConnId);
            
            if (oldState == null) {
                log.warn("Old connection state not found for reconnection: oldConnId={}, newConnId={}", 
                        oldConnId, newConnId);
                return false;
            }
            
            // 获取设备ID
            long deviceId = oldState.getDeviceId();
            
            // 创建新的连接状态
            ConnState newState = newConnState(deviceId, newConnId);
            
            // 添加路由记录
            String endpoint = serviceAddress + ":" + servicePort;
            boolean success = routerTable.addRecord(deviceId, endpoint, newConnId);
            
            if (!success) {
                log.warn("Failed to add route record during reconnection: deviceId={}, oldConnId={}, newConnId={}", 
                        deviceId, oldConnId, newConnId);
                return false;
            }
            
            // 关闭旧连接状态
            oldState.close();
            
            // 移除旧连接状态
            deleteConnIdState(oldConnId);
            
            log.info("Reconnection successful: deviceId={}, oldConnId={}, newConnId={}", 
                    deviceId, oldConnId, newConnId);
            
            return true;
        } catch (Exception e) {
            log.error("Error during reconnection: oldConnId={}, newConnId={}", oldConnId, newConnId, e);
            return false;
        }
    }

    /**
     * 重置心跳定时器
     * 对应Go版本中的resetHeartTimer函数
     *
     * @param connId 连接ID
     */
    public void resetHeartTimer(long connId) {
        try {
            // 获取连接状态
            ConnState state = loadConnIdState(connId);
            
            if (state == null) {
                log.warn("Connection state not found for resetting heart timer: connId={}", connId);
                return;
            }
            
            // 重置心跳定时器
            state.resetHeartTimer();
            
            log.debug("Reset heart timer: connId={}", connId);
        } catch (Exception e) {
            log.error("Error resetting heart timer: connId={}", connId, e);
        }
    }

    /**
     * 加载连接ID状态
     * 对应Go版本中的loadConnIdState函数
     *
     * @param connId 连接ID
     * @return 连接状态
     */
    public ConnState loadConnIdState(long connId) {
        // 先从本地缓存获取
        ConnState state = connStateMap.get(connId);
        
        // 如果本地缓存中不存在，则从Redis获取
        if (state == null) {
            String key = redisShardConfig.getConnStateKey(connId);
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value instanceof ConnState) {
                state = (ConnState) value;
                // 更新本地缓存
                connStateMap.put(connId, state);
            }
        }
        
        return state;
    }

    /**
     * 删除连接ID状态
     * 对应Go版本中的deleteConnIdState函数
     *
     * @param connId 连接ID
     */
    public void deleteConnIdState(long connId) {
        // 从本地缓存中删除
        connStateMap.remove(connId);
        
        // 从Redis中删除
        String key = redisShardConfig.getConnStateKey(connId);
        redisTemplate.delete(key);
        
        log.debug("Deleted connection state: connId={}", connId);
    }

    /**
     * 存储连接ID状态
     * 对应Go版本中的storeConnIdState函数
     *
     * @param connId 连接ID
     * @param state  连接状态
     */
    public void storeConnIdState(long connId, ConnState state) {
        // 存储到本地缓存
        connStateMap.put(connId, state);
        
        // 存储到Redis
        String key = redisShardConfig.getConnStateKey(connId);
        redisTemplate.opsForValue().set(key, state);
        
        log.debug("Stored connection state: connId={}", connId);
    }

    /**
     * 获取登录槽键
     * 对应Go版本中的getLoginSlotKey函数
     *
     * @param connId 连接ID
     * @return 登录槽键
     */
    public String getLoginSlotKey(long connId) {
        // 计算槽
        long slot = getConnStateSlot(connId);
        
        // 构建登录槽键
        return "login_slot:" + slot;
    }

    /**
     * 获取连接状态槽
     * 对应Go版本中的getConnStateSlot函数
     *
     * @param connId 连接ID
     * @return 槽
     */
    public long getConnStateSlot(long connId) {
        // 计算槽
        return connId % 1024;
    }

    /**
     * 比较并递增客户端ID
     * 注意：此方法已被MessageStateMachine替代，保留是为了兼容性
     *
     * @param connId       连接ID
     * @param oldMaxClientId 旧的最大客户端ID
     * @param sessionId    会话ID
     * @return 是否成功
     */
    @Deprecated
    public boolean compareAndIncrClientId(long connId, long oldMaxClientId, String sessionId) {
        try {
            // 构建键
            String key = redisShardConfig.getClientIdKey(connId, sessionId);
            
            // 执行Lua脚本
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(COMPARE_AND_INCR_CLIENT_ID_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(key), String.valueOf(oldMaxClientId));
            
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Error comparing and incrementing client ID: connId={}, oldMaxClientId={}, sessionId={}", 
                    connId, oldMaxClientId, sessionId, e);
            return false;
        }
    }

    /**
     * 添加最后一条消息
     * 对应Go版本中的appendLastMsg函数
     *
     * @param connId      连接ID
     * @param pushMessage 推送消息
     * @return 是否成功
     */
    public boolean appendLastMsg(long connId, PushMessage pushMessage) {
        try {
            // 获取连接状态
            ConnState state = loadConnIdState(connId);
            
            if (state == null) {
                log.warn("Connection state not found for appending last message: connId={}", connId);
                return false;
            }
            
            // 构建键
            String key = redisShardConfig.getLastMsgKey(connId);
            
            // 构建消息定时器锁
            String msgTimerLock = redisShardConfig.getMsgTimerLockKey(connId);
            
            // 序列化推送消息
            byte[] msgData = serializePushMessage(pushMessage);
            
            // 添加消息
            state.appendMsg(key, msgTimerLock, msgData);
            
            log.debug("Appended last message: connId={}, msgId={}", connId, pushMessage.getMsgId());
            
            return true;
        } catch (Exception e) {
            log.error("Error appending last message: connId={}", connId, e);
            return false;
        }
    }

    /**
     * 确认最后一条消息
     * 对应Go版本中的ackLastMsg函数
     *
     * @param connId    连接ID
     * @param sessionId 会话ID
     * @param msgId     消息ID
     */
    public void ackLastMsg(long connId, long sessionId, long msgId) {
        try {
            // 获取连接状态
            ConnState state = loadConnIdState(connId);
            
            if (state == null) {
                log.warn("Connection state not found for acknowledging last message: connId={}", connId);
                return;
            }
            
            // 确认最后一条消息
            boolean success = state.ackLastMsg(sessionId, msgId);
            
            if (success) {
                log.debug("Acknowledged last message: connId={}, sessionId={}, msgId={}", connId, sessionId, msgId);
            } else {
                log.warn("Failed to acknowledge last message: connId={}, sessionId={}, msgId={}", 
                        connId, sessionId, msgId);
            }
        } catch (Exception e) {
            log.error("Error acknowledging last message: connId={}, sessionId={}, msgId={}", 
                    connId, sessionId, msgId, e);
        }
    }

    /**
     * 获取最后一条消息
     * 对应Go版本中的getLastMsg函数
     *
     * @param connId 连接ID
     * @return 推送消息
     */
    public PushMessage getLastMsg(long connId) {
        try {
            // 构建键
            String key = redisShardConfig.getLastMsgKey(connId);
            
            // 获取最后一条消息
            byte[] msgData = (byte[]) redisTemplate.opsForValue().get(key);
            
            if (msgData == null) {
                return null;
            }
            
            // 反序列化推送消息
            return deserializePushMessage(msgData);
        } catch (Exception e) {
            log.error("Error getting last message: connId={}", connId, e);
            return null;
        }
    }

    /**
     * 登录槽反序列化
     * 对应Go版本中的loginSlotUnmarshal函数
     *
     * @param meta 元数据
     * @return 设备ID和连接ID
     */
    public long[] loginSlotUnmarshal(String meta) {
        try {
            // 分割元数据
            String[] parts = meta.split(":");
            
            if (parts.length != 2) {
                log.warn("Invalid login slot meta format: {}", meta);
                return new long[]{0, 0};
            }
            
            // 解析设备ID和连接ID
            long deviceId = Long.parseLong(parts[0]);
            long connId = Long.parseLong(parts[1]);
            
            return new long[]{deviceId, connId};
        } catch (Exception e) {
            log.error("Error unmarshalling login slot: meta={}", meta, e);
            return new long[]{0, 0};
        }
    }

    /**
     * 登录槽序列化
     * 对应Go版本中的loginSlotMarshal函数
     *
     * @param deviceId 设备ID
     * @param connId   连接ID
     * @return 元数据
     */
    public String loginSlotMarshal(long deviceId, long connId) {
        return deviceId + ":" + connId;
    }

    /**
     * 添加路由记录
     * 对应Go版本中的addRouteRecord函数
     * 注意：此方法已被RouterTable替代，保留是为了兼容性
     *
     * @param deviceId 设备ID
     * @param endpoint 端点
     * @param connId   连接ID
     * @return 是否成功
     */
    @Deprecated
    public boolean addRouteRecord(long deviceId, String endpoint, long connId) {
        return routerTable.addRecord(deviceId, endpoint, connId);
    }

    /**
     * 删除路由记录
     * 对应Go版本中的deleteRouteRecord函数
     * 注意：此方法已被RouterTable替代，保留是为了兼容性
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    @Deprecated
    public boolean deleteRouteRecord(long deviceId) {
        return routerTable.deleteRecord(deviceId);
    }

    /**
     * 通知网关删除连接
     * 对应Go版本中的notifyGatewayDeleteConn函数
     *
     * @param connId 连接ID
     * @return 是否成功
     */
    public boolean notifyGatewayDeleteConn(long connId) {
        try {
            // 通知网关删除连接
            boolean success = gatewayClient.delConn(connId, null);
            
            log.info("Notified gateway to delete connection: connId={}, success={}", connId, success);
            
            return success;
        } catch (Exception e) {
            log.error("Error notifying gateway to delete connection: connId={}", connId, e);
            return false;
        }
    }

    /**
     * 重新推送
     * 对应Go版本中的rePush函数
     *
     * @param connId 连接ID
     */
    public void rePush(long connId) {
        try {
            // 获取最后一条消息
            PushMessage lastMsg = getLastMsg(connId);
            
            if (lastMsg == null) {
                log.debug("No last message to re-push: connId={}", connId);
                return;
            }
            
            // 构建推送消息
            plato.common.message.MessageOuterClass.PushMsg pushMsg = 
                    plato.common.message.MessageOuterClass.PushMsg.newBuilder()
                    .setContent(com.google.protobuf.ByteString.copyFrom(lastMsg.getContent()))
                    .setMsgID(lastMsg.getMsgId())
                    .setSessionID(lastMsg.getSessionId())
                    .build();
            
            // 构建消息命令
            plato.common.message.MessageOuterClass.MsgCmd msgCmd = 
                    plato.common.message.MessageOuterClass.MsgCmd.newBuilder()
                    .setType(plato.common.message.MessageOuterClass.CmdType.Push)
                    .setPayload(com.google.protobuf.ByteString.copyFrom(pushMsg.toByteArray()))
                    .build();
            
            // 序列化消息命令
            byte[] data = msgCmd.toByteArray();
            
            // 发送消息
            boolean success = gatewayClient.push(connId, data);
            
            log.info("Re-pushed message: connId={}, msgId={}, success={}", 
                    connId, lastMsg.getMsgId(), success);
        } catch (Exception e) {
            log.error("Error re-pushing message: connId={}", connId, e);
        }
    }

    /**
     * 序列化推送消息
     * 对应Go版本中的serializePushMessage函数
     *
     * @param pushMessage 推送消息
     * @return 序列化后的数据
     */
    private byte[] serializePushMessage(PushMessage pushMessage) {
        try {
            // 使用Java序列化
            return redisTemplate.getValueSerializer().serialize(pushMessage);
        } catch (Exception e) {
            log.error("Error serializing push message", e);
            return new byte[0];
        }
    }

    /**
     * 反序列化推送消息
     * 对应Go版本中的deserializePushMessage函数
     *
     * @param data 序列化后的数据
     * @return 推送消息
     */
    private PushMessage deserializePushMessage(byte[] data) {
        try {
            // 使用Java反序列化
            return (PushMessage) redisTemplate.getValueSerializer().deserialize(data);
        } catch (Exception e) {
            log.error("Error deserializing push message", e);
            return null;
        }
    }
    
    /**
     * 生成全局唯一的连接ID
     * 使用雪花算法生成
     *
     * @return 连接ID
     */
    public long generateConnId() {
        return snowflakeIdGenerator.nextId();
    }
    
    /**
     * 判断当前实例是否负责指定的连接ID
     *
     * @param connId 连接ID
     * @return 是否负责
     */
    public boolean isResponsibleForConn(long connId) {
        int shardId = redisShardConfig.getShardId(connId);
        return redisShardConfig.isResponsibleForShard(shardId);
    }
} 