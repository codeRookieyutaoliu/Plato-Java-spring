package plato.state.domain.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import plato.state.infrastructure.timer.TimerManager;
import plato.state.infrastructure.timer.TimingWheel;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 连接状态
 * 表示一个长连接的状态信息
 * 对应Go版本中的connState结构体
 */
@Slf4j
@Getter
public class ConnState {

    /**
     * 读写锁
     * 对应Go版本中的sync.RWMutex
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 心跳定时器
     * 对应Go版本中的heartTimer
     */
    private TimingWheel.Timer heartTimer;

    /**
     * 重连定时器
     * 对应Go版本中的reConnTimer
     */
    private TimingWheel.Timer reConnTimer;

    /**
     * 消息定时器
     * 对应Go版本中的msgTimer
     */
    private TimingWheel.Timer msgTimer;

    /**
     * 消息定时器锁
     * 对应Go版本中的msgTimerLock
     */
    private String msgTimerLock;

    /**
     * 连接ID
     * 对应Go版本中的connID
     */
    private final long connId;

    /**
     * 设备ID
     * 对应Go版本中的did
     */
    private final long deviceId;

    /**
     * 定时器管理器
     */
    private final TimerManager timerManager;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存状态管理器
     */
    private final CacheStateManager cacheStateManager;

    /**
     * 构造函数
     *
     * @param connId            连接ID
     * @param deviceId          设备ID
     * @param timerManager      定时器管理器
     * @param redisTemplate     Redis模板
     * @param cacheStateManager 缓存状态管理器
     */
    public ConnState(long connId, long deviceId, TimerManager timerManager, 
                    RedisTemplate<String, Object> redisTemplate,
                    CacheStateManager cacheStateManager) {
        this.connId = connId;
        this.deviceId = deviceId;
        this.timerManager = timerManager;
        this.redisTemplate = redisTemplate;
        this.cacheStateManager = cacheStateManager;
    }

    /**
     * 关闭连接状态
     * 对应Go版本中的close方法
     *
     * @return 是否成功
     */
    public boolean close() {
        lock.writeLock().lock();
        try {
            // 停止所有定时器
            if (heartTimer != null) {
                heartTimer.stop();
                heartTimer = null;
            }
            if (reConnTimer != null) {
                reConnTimer.stop();
                reConnTimer = null;
            }
            if (msgTimer != null) {
                msgTimer.stop();
                msgTimer = null;
            }

            // 从登录槽中移除
            String slotKey = cacheStateManager.getLoginSlotKey(connId);
            String meta = cacheStateManager.loginSlotMarshal(deviceId, connId);
            redisTemplate.opsForSet().remove(slotKey, meta);

            // 获取连接状态槽
            long slot = cacheStateManager.getConnStateSlot(connId);

            // 删除客户端ID相关的键
            String keyPattern = String.format("max_client_id:%d:%d:*", slot, connId);
            redisTemplate.delete(redisTemplate.keys(keyPattern));

            // 删除路由记录
            cacheStateManager.deleteRouteRecord(deviceId);

            // 删除最后一条消息
            String lastMsgKey = String.format("last_msg:%d:%d", slot, connId);
            redisTemplate.delete(lastMsgKey);

            // 通知网关删除连接
            cacheStateManager.notifyGatewayDeleteConn(connId);

            // 从连接状态表中删除
            cacheStateManager.deleteConnIdState(connId);

            log.info("Connection state closed: connId={}, deviceId={}", connId, deviceId);
            return true;
        } catch (Exception e) {
            log.error("Error closing connection state: connId={}, deviceId={}", connId, deviceId, e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 添加消息
     * 对应Go版本中的appendMsg方法
     *
     * @param key          缓存键
     * @param msgTimerLock 消息定时器锁
     * @param msgData      消息数据
     */
    public void appendMsg(String key, String msgTimerLock, byte[] msgData) {
        lock.writeLock().lock();
        try {
            this.msgTimerLock = msgTimerLock;
            if (msgTimer != null) {
                msgTimer.stop();
                msgTimer = null;
            }

            // 创建定时器，100毫秒后重新推送消息
            msgTimer = timerManager.afterFunc(100, () -> cacheStateManager.rePush(connId));

            // 存储消息到Redis
            redisTemplate.opsForValue().set(key, msgData, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Error appending message: connId={}, key={}", connId, key, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 重置消息定时器
     * 对应Go版本中的reSetMsgTimer方法
     *
     * @param connId    连接ID
     * @param sessionId 会话ID
     * @param msgId     消息ID
     */
    public void resetMsgTimer(long connId, long sessionId, long msgId) {
        lock.writeLock().lock();
        try {
            if (msgTimer != null) {
                msgTimer.stop();
            }
            msgTimerLock = String.format("%d_%d", sessionId, msgId);
            msgTimer = timerManager.afterFunc(100, () -> cacheStateManager.rePush(connId));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 加载消息定时器
     * 对应Go版本中的loadMsgTimer方法
     */
    public void loadMsgTimer() {
        try {
            // 获取最后一条消息
            PushMessage lastMsg = cacheStateManager.getLastMsg(connId);
            if (lastMsg != null) {
                resetMsgTimer(connId, lastMsg.getSessionId(), lastMsg.getMsgId());
            }
        } catch (Exception e) {
            log.error("Error loading message timer: connId={}", connId, e);
        }
    }

    /**
     * 重置心跳定时器
     * 对应Go版本中的reSetHeartTimer方法
     */
    public void resetHeartTimer() {
        lock.writeLock().lock();
        try {
            if (heartTimer != null) {
                heartTimer.stop();
            }
            // 创建心跳定时器，5秒后触发重连定时器
            heartTimer = timerManager.afterFunc(5, TimeUnit.SECONDS, this::resetReConnTimer);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 重置重连定时器
     * 对应Go版本中的reSetReConnTimer方法
     */
    public void resetReConnTimer() {
        lock.writeLock().lock();
        try {
            if (reConnTimer != null) {
                reConnTimer.stop();
            }
            // 创建重连定时器，10秒后注销连接
            reConnTimer = timerManager.afterFunc(10, TimeUnit.SECONDS, () -> 
                cacheStateManager.connLogOut(connId));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 确认最后一条消息
     * 对应Go版本中的ackLastMsg方法
     *
     * @param sessionId 会话ID
     * @param msgId     消息ID
     * @return 是否成功
     */
    public boolean ackLastMsg(long sessionId, long msgId) {
        lock.writeLock().lock();
        try {
            String expectedLock = String.format("%d_%d", sessionId, msgId);
            if (!expectedLock.equals(msgTimerLock)) {
                return false;
            }

            // 删除最后一条消息
            long slot = cacheStateManager.getConnStateSlot(connId);
            String key = String.format("last_msg:%d:%d", slot, connId);
            redisTemplate.delete(key);

            // 停止消息定时器
            if (msgTimer != null) {
                msgTimer.stop();
                msgTimer = null;
            }

            return true;
        } catch (Exception e) {
            log.error("Error acknowledging last message: connId={}, sessionId={}, msgId={}", 
                    connId, sessionId, msgId, e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
} 