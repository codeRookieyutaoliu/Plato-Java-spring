package plato.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis缓存服务实现类
 * 基于Spring Data Redis实现缓存操作
 */
@Slf4j
@Component
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisCacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        
        if (value instanceof String && clazz != String.class) {
            try {
                return objectMapper.readValue((String) value, clazz);
            } catch (JsonProcessingException e) {
                log.error("反序列化缓存值失败，键：{}，类型：{}", key, clazz.getName(), e);
                return null;
            }
        }
        
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        
        return null;
    }

    @Override
    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public long delete(List<String> keys) {
        Long result = redisTemplate.delete(keys);
        return result != null ? result : 0;
    }

    @Override
    public boolean expire(String key, Duration timeout) {
        Boolean result = redisTemplate.expire(key, timeout);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public long getExpire(String key) {
        Long result = redisTemplate.getExpire(key);
        return result != null ? result : -1;
    }

    @Override
    public boolean hasKey(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public long incr(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    @Override
    public long decr(String key, long delta) {
        Long result = redisTemplate.opsForValue().decrement(key, delta);
        return result != null ? result : 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String hashKey, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(key, hashKey);
        if (value == null) {
            return null;
        }
        
        if (value instanceof String && clazz != String.class) {
            try {
                return objectMapper.readValue((String) value, clazz);
            } catch (JsonProcessingException e) {
                log.error("反序列化Hash缓存值失败，键：{}，Hash键：{}，类型：{}", key, hashKey, clazz.getName(), e);
                return null;
            }
        }
        
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        
        return null;
    }

    @Override
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public void hSet(String key, String hashKey, Object value, Duration timeout) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        expire(key, timeout);
    }

    @Override
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public long hDelete(String key, Object... hashKeys) {
        Long result = redisTemplate.opsForHash().delete(key, hashKeys);
        return result != null ? result : 0;
    }

    @Override
    public boolean hHasKey(String key, String hashKey) {
        Boolean result = redisTemplate.opsForHash().hasKey(key, hashKey);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public long sAdd(String key, Object... values) {
        Long result = redisTemplate.opsForSet().add(key, values);
        return result != null ? result : 0;
    }

    @Override
    public long sAdd(String key, Duration timeout, Object... values) {
        Long result = redisTemplate.opsForSet().add(key, values);
        expire(key, timeout);
        return result != null ? result : 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> sMembers(String key, Class<T> clazz) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) {
            return Set.of();
        }
        
        return members.stream()
                .filter(member -> clazz.isInstance(member) || member instanceof String)
                .map(member -> {
                    if (member instanceof String && clazz != String.class) {
                        try {
                            return objectMapper.readValue((String) member, clazz);
                        } catch (JsonProcessingException e) {
                            log.error("反序列化Set缓存值失败，键：{}，类型：{}", key, clazz.getName(), e);
                            return null;
                        }
                    }
                    return (T) member;
                })
                .filter(member -> member != null)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean sIsMember(String key, Object value) {
        Boolean result = redisTemplate.opsForSet().isMember(key, value);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public long sSize(String key) {
        Long result = redisTemplate.opsForSet().size(key);
        return result != null ? result : 0;
    }

    @Override
    public long sRemove(String key, Object... values) {
        Long result = redisTemplate.opsForSet().remove(key, values);
        return result != null ? result : 0;
    }

    @Override
    public long lPush(String key, Object value) {
        Long result = redisTemplate.opsForList().rightPush(key, value);
        return result != null ? result : 0;
    }

    @Override
    public long lPush(String key, Object value, Duration timeout) {
        Long result = redisTemplate.opsForList().rightPush(key, value);
        expire(key, timeout);
        return result != null ? result : 0;
    }

    @Override
    public long lPushAll(String key, Object... values) {
        Long result = redisTemplate.opsForList().rightPushAll(key, values);
        return result != null ? result : 0;
    }

    @Override
    public long lPushAll(String key, Duration timeout, Object... values) {
        Long result = redisTemplate.opsForList().rightPushAll(key, values);
        expire(key, timeout);
        return result != null ? result : 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(String key, long start, long end, Class<T> clazz) {
        List<Object> range = redisTemplate.opsForList().range(key, start, end);
        if (range == null) {
            return List.of();
        }
        
        return range.stream()
                .filter(item -> clazz.isInstance(item) || item instanceof String)
                .map(item -> {
                    if (item instanceof String && clazz != String.class) {
                        try {
                            return objectMapper.readValue((String) item, clazz);
                        } catch (JsonProcessingException e) {
                            log.error("反序列化List缓存值失败，键：{}，类型：{}", key, clazz.getName(), e);
                            return null;
                        }
                    }
                    return (T) item;
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    @Override
    public long lSize(String key) {
        Long result = redisTemplate.opsForList().size(key);
        return result != null ? result : 0;
    }

    @Override
    public long lRemove(String key, long count, Object value) {
        Long result = redisTemplate.opsForList().remove(key, count, value);
        return result != null ? result : 0;
    }
} 