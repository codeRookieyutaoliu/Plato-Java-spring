package plato.state.domain.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import plato.common.id.SnowflakeIdGenerator;
import plato.state.config.MessageConfig;
import plato.state.config.RedisShardConfig;
import plato.state.infrastructure.timer.TimerManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 消息状态机
 * 负责处理消息的可靠性，包括上行消息和下行消息的可靠性保证
 * 对应Go版本中的msgStateMachine结构体及相关函数
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageStateMachine {

    /**
     * 连接ID与客户端ID的本地缓存
     * 对应Go版本中的connClientIdMap
     */
    private final Map<Long, Long> connClientIdMap = new ConcurrentHashMap<>();

    /**
     * 连接ID与会话ID的本地缓存
     * 对应Go版本中的connSessionIdMap
     */
    private final Map<Long, String> connSessionIdMap = new ConcurrentHashMap<>();

    /**
     * Redis模板
     * 用于存储消息状态
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 定时器管理器
     * 用于设置消息重传定时器
     */
    private final TimerManager timerManager;

    /**
     * 缓存状态管理器
     * 用于管理连接状态
     */
    private final CacheStateManager cacheStateManager;

    /**
     * 消息配置
     * 包含消息重传间隔、最大重传次数等配置
     */
    private final MessageConfig messageConfig;
    
    /**
     * Redis分片配置
     * 用于配置Redis分片存储
     */
    private final RedisShardConfig redisShardConfig;
    
    /**
     * 雪花算法ID生成器
     * 用于生成全局唯一的消息ID
     */
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 生成新的消息ID
     * 使用雪花算法生成全局唯一ID
     * 对应Go版本中的generateMsgId函数，但使用了更可靠的雪花算法
     *
     * @return 新的消息ID
     */
    public long generateMsgId() {
        return snowflakeIdGenerator.nextId();
    }

    /**
     * 记录客户端ID
     * 对应Go版本中的recordClientId函数
     *
     * @param connId   连接ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     */
    public void recordClientId(long connId, long clientId, String sessionId) {
        // 记录客户端ID到本地缓存
        connClientIdMap.put(connId, clientId);
        connSessionIdMap.put(connId, sessionId);
        
        // 记录客户端ID到Redis
        String key = redisShardConfig.getClientIdKey(connId, sessionId);
        redisTemplate.opsForValue().set(key, clientId, messageConfig.getExpireTime(), TimeUnit.SECONDS);
        
        log.debug("Recorded client ID: connId={}, clientId={}, sessionId={}", connId, clientId, sessionId);
    }

    /**
     * 获取客户端ID
     * 对应Go版本中的getClientId函数
     *
     * @param connId 连接ID
     * @return 客户端ID，如果不存在则返回0
     */
    public long getClientId(long connId) {
        // 先从本地缓存获取
        Long clientId = connClientIdMap.get(connId);
        
        // 如果本地缓存中不存在，则从Redis获取
        if (clientId == null) {
            String sessionId = getSessionId(connId);
            if (!sessionId.isEmpty()) {
                String key = redisShardConfig.getClientIdKey(connId, sessionId);
                Object value = redisTemplate.opsForValue().get(key);
                if (value instanceof Long) {
                    clientId = (Long) value;
                    // 更新本地缓存
                    connClientIdMap.put(connId, clientId);
                }
            }
        }
        
        return clientId != null ? clientId : 0L;
    }

    /**
     * 获取会话ID
     * 对应Go版本中的getSessionId函数
     *
     * @param connId 连接ID
     * @return 会话ID，如果不存在则返回空字符串
     */
    public String getSessionId(long connId) {
        return connSessionIdMap.getOrDefault(connId, "");
    }

    /**
     * 比较并递增客户端ID
     * 对应Go版本中的compareAndIncrClientId函数
     *
     * @param connId       连接ID
     * @param clientId     客户端ID
     * @param sessionId    会话ID
     * @return 比较结果，true表示客户端ID有效且已更新，false表示无效
     */
    public boolean compareAndIncrClientId(long connId, long clientId, String sessionId) {
        // 获取当前记录的客户端ID
        Long currentClientId = connClientIdMap.get(connId);
        
        // 如果本地缓存中不存在，则从Redis获取
        if (currentClientId == null) {
            String key = redisShardConfig.getClientIdKey(connId, sessionId);
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof Long) {
                currentClientId = (Long) value;
                // 更新本地缓存
                connClientIdMap.put(connId, currentClientId);
            }
        }
        
        // 如果没有记录，则直接记录并返回true
        if (currentClientId == null) {
            recordClientId(connId, clientId, sessionId);
            return true;
        }
        
        // 如果新的客户端ID小于等于当前记录的客户端ID，则认为是重复消息，返回false
        if (clientId <= currentClientId) {
            log.warn("Duplicate message detected: connId={}, currentClientId={}, receivedClientId={}", 
                    connId, currentClientId, clientId);
            return false;
        }
        
        // 如果新的客户端ID大于当前记录的客户端ID，则更新记录并返回true
        recordClientId(connId, clientId, sessionId);
        return true;
    }

    /**
     * 删除连接的客户端ID记录
     * 对应Go版本中的deleteConnClientId函数
     *
     * @param connId 连接ID
     */
    public void deleteConnClientId(long connId) {
        // 获取会话ID
        String sessionId = connSessionIdMap.get(connId);
        
        // 从本地缓存中删除
        connClientIdMap.remove(connId);
        connSessionIdMap.remove(connId);
        
        // 从Redis中删除
        if (sessionId != null && !sessionId.isEmpty()) {
            String key = redisShardConfig.getClientIdKey(connId, sessionId);
            redisTemplate.delete(key);
        }
        
        log.debug("Deleted client ID record: connId={}", connId);
    }

    /**
     * 处理上行消息
     * 对应Go版本中的handleUpMsg函数
     *
     * @param connId    连接ID
     * @param clientId  客户端ID
     * @param sessionId 会话ID
     * @param payload   消息负载
     * @return 处理结果，true表示成功，false表示失败
     */
    public boolean handleUpMsg(long connId, long clientId, String sessionId, byte[] payload) {
        // 比较并递增客户端ID，确保消息不重复
        if (!compareAndIncrClientId(connId, clientId, sessionId)) {
            log.warn("Failed to handle up message due to invalid client ID: connId={}, clientId={}", connId, clientId);
            return false;
        }
        
        // 这里应该调用业务层处理上行消息
        // 在实际应用中，可能需要将消息发送到消息队列或其他业务处理系统
        log.info("Handled up message: connId={}, clientId={}, sessionId={}, payloadSize={}", 
                connId, clientId, sessionId, payload.length);
        
        // 生成新的消息ID用于下行消息
        long msgId = generateMsgId();
        
        // 模拟业务处理后的下行消息
        handleDownMsg(connId, msgId, sessionId, payload);
        
        return true;
    }

    /**
     * 处理下行消息
     * 对应Go版本中的handleDownMsg函数
     *
     * @param connId    连接ID
     * @param msgId     消息ID
     * @param sessionId 会话ID
     * @param payload   消息负载
     * @return 处理结果，true表示成功，false表示失败
     */
    public boolean handleDownMsg(long connId, long msgId, String sessionId, byte[] payload) {
        try {
            // 创建推送消息
            PushMessage pushMessage = PushMessage.builder()
                    .msgId(msgId)
                    .sessionId(Long.parseLong(sessionId))
                    .content(payload)
                    .build();
            
            // 将消息添加到最后一条消息缓存
            boolean success = cacheStateManager.appendLastMsg(connId, pushMessage);
            
            if (success) {
                log.info("Handled down message: connId={}, msgId={}, sessionId={}, payloadSize={}", 
                        connId, msgId, sessionId, payload.length);
                
                // 设置消息重传定时器
                setMsgRetryTimer(connId, msgId, sessionId, payload, 0);
            } else {
                log.warn("Failed to append last message: connId={}, msgId={}", connId, msgId);
            }
            
            return success;
        } catch (Exception e) {
            log.error("Error handling down message: connId={}, msgId={}", connId, msgId, e);
            return false;
        }
    }

    /**
     * 设置消息重传定时器
     * 对应Go版本中的setMsgRetryTimer函数
     *
     * @param connId    连接ID
     * @param msgId     消息ID
     * @param sessionId 会话ID
     * @param payload   消息负载
     * @param retryTimes 重试次数
     */
    private void setMsgRetryTimer(long connId, long msgId, String sessionId, byte[] payload, int retryTimes) {
        // 如果已经达到最大重试次数，则不再重试
        if (retryTimes >= messageConfig.getMaxRetryTimes()) {
            log.warn("Message retry reached max times: connId={}, msgId={}, retryTimes={}", 
                    connId, msgId, retryTimes);
            return;
        }
        
        // 创建重传任务
        Runnable retryTask = () -> {
            // 检查消息是否已经被确认
            if (isMessageAcknowledged(connId, msgId)) {
                log.debug("Message already acknowledged, no need to retry: connId={}, msgId={}", connId, msgId);
                return;
            }
            
            // 重新发送消息
            log.info("Retrying message: connId={}, msgId={}, retryTimes={}", connId, msgId, retryTimes + 1);
            
            // 重新发送消息
            cacheStateManager.rePush(connId);
            
            // 设置下一次重传定时器
            setMsgRetryTimer(connId, msgId, sessionId, payload, retryTimes + 1);
        };
        
        // 设置定时器
        timerManager.afterFunc(messageConfig.getRetryInterval(), retryTask);
    }

    /**
     * 检查消息是否已经被确认
     * 对应Go版本中的isMessageAcked函数
     *
     * @param connId 连接ID
     * @param msgId  消息ID
     * @return 是否已确认
     */
    private boolean isMessageAcknowledged(long connId, long msgId) {
        // 获取最后一条消息
        PushMessage lastMsg = cacheStateManager.getLastMsg(connId);
        
        // 如果没有最后一条消息，或者最后一条消息的ID大于当前消息ID，则认为当前消息已经被确认
        return lastMsg == null || lastMsg.getMsgId() > msgId;
    }

    /**
     * 确认消息
     * 对应Go版本中的ackMessage函数
     *
     * @param connId    连接ID
     * @param sessionId 会话ID
     * @param msgId     消息ID
     * @return 确认结果，true表示成功，false表示失败
     */
    public boolean ackMessage(long connId, String sessionId, long msgId) {
        try {
            // 确认最后一条消息
            cacheStateManager.ackLastMsg(connId, Long.parseLong(sessionId), msgId);
            
            log.debug("Acknowledged message: connId={}, sessionId={}, msgId={}", connId, sessionId, msgId);
            return true;
        } catch (Exception e) {
            log.error("Error acknowledging message: connId={}, sessionId={}, msgId={}", connId, sessionId, msgId, e);
            return false;
        }
    }
} 