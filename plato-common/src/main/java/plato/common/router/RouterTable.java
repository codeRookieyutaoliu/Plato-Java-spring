package plato.common.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 路由表
 * 管理设备与连接的路由信息
 * 对应Go版本中的router包中的功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouterTable {

    /**
     * 路由表键前缀
     * 对应Go版本中的gatewayRotuerKey常量
     */
    private static final String GATEWAY_ROUTER_KEY = "gateway_router_%d";

    /**
     * 路由表过期时间（7天）
     * 对应Go版本中的ttl7D常量
     */
    private static final long TTL_7D = 7 * 24 * 60 * 60;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 添加路由记录
     * 对应Go版本中的AddRecord函数
     *
     * @param deviceId 设备ID
     * @param endpoint 端点地址
     * @param connId   连接ID
     * @return 是否成功
     */
    public boolean addRecord(long deviceId, String endpoint, long connId) {
        try {
            String key = String.format(GATEWAY_ROUTER_KEY, deviceId);
            String value = String.format("%s-%d", endpoint, connId);
            redisTemplate.opsForValue().set(key, value, TTL_7D, TimeUnit.SECONDS);
            log.debug("Added router record: deviceId={}, endpoint={}, connId={}", deviceId, endpoint, connId);
            return true;
        } catch (Exception e) {
            log.error("Failed to add router record: deviceId={}, endpoint={}, connId={}", deviceId, endpoint, connId, e);
            return false;
        }
    }

    /**
     * 删除路由记录
     * 对应Go版本中的DelRecord函数
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    public boolean deleteRecord(long deviceId) {
        try {
            String key = String.format(GATEWAY_ROUTER_KEY, deviceId);
            Boolean result = redisTemplate.delete(key);
            log.debug("Deleted router record: deviceId={}, success={}", deviceId, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to delete router record: deviceId={}", deviceId, e);
            return false;
        }
    }

    /**
     * 查询路由记录
     * 对应Go版本中的QueryRecord函数
     *
     * @param deviceId 设备ID
     * @return 路由记录，如果不存在则返回null
     */
    public RouterRecord queryRecord(long deviceId) {
        try {
            String key = String.format(GATEWAY_ROUTER_KEY, deviceId);
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                log.debug("Router record not found: deviceId={}", deviceId);
                return null;
            }
            
            String[] parts = value.split("-");
            if (parts.length != 2) {
                log.warn("Invalid router record format: deviceId={}, value={}", deviceId, value);
                return null;
            }
            
            String endpoint = parts[0];
            long connId = Long.parseLong(parts[1]);
            
            RouterRecord record = new RouterRecord(endpoint, connId);
            log.debug("Queried router record: deviceId={}, endpoint={}, connId={}", deviceId, endpoint, connId);
            
            return record;
        } catch (Exception e) {
            log.error("Failed to query router record: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 批量删除路由记录
     *
     * @param deviceIds 设备ID列表
     * @return 成功删除的记录数
     */
    public int deleteRecords(long[] deviceIds) {
        if (deviceIds == null || deviceIds.length == 0) {
            return 0;
        }
        
        int successCount = 0;
        for (long deviceId : deviceIds) {
            if (deleteRecord(deviceId)) {
                successCount++;
            }
        }
        
        log.debug("Deleted multiple router records: total={}, success={}", deviceIds.length, successCount);
        return successCount;
    }

    /**
     * 更新路由记录
     *
     * @param deviceId 设备ID
     * @param endpoint 端点地址
     * @param connId   连接ID
     * @return 是否成功
     */
    public boolean updateRecord(long deviceId, String endpoint, long connId) {
        return addRecord(deviceId, endpoint, connId);
    }

    /**
     * 检查路由记录是否存在
     *
     * @param deviceId 设备ID
     * @return 是否存在
     */
    public boolean existsRecord(long deviceId) {
        try {
            String key = String.format(GATEWAY_ROUTER_KEY, deviceId);
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check router record existence: deviceId={}", deviceId, e);
            return false;
        }
    }
} 