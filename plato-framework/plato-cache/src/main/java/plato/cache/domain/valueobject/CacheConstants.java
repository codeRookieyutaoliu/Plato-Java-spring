package plato.cache.domain.valueobject;

import java.time.Duration;

/**
 * 缓存常量定义
 * 包含缓存键模板和过期时间等常量
 * 对应Go项目中的const.go文件
 */
public class CacheConstants {

    /**
     * 最大客户端ID缓存键模板
     * 对应Go项目中的MaxClientIDKey常量
     */
    public static final String MAX_CLIENT_ID_KEY = "max_client_id_{%d}_%d_%s";
    
    /**
     * 最后消息缓存键模板
     * 对应Go项目中的LastMsgKey常量
     */
    public static final String LAST_MSG_KEY = "last_msg_{%d}_%d";
    
    /**
     * 登录槽集合缓存键模板
     * 对应Go项目中的LoginSlotSetKey常量
     */
    public static final String LOGIN_SLOT_SET_KEY = "login_slot_set_{%d}";
    
    /**
     * 网关路由缓存键模板
     * 对应Go项目中router包的gatewayRotuerKey常量
     */
    public static final String GATEWAY_ROUTER_KEY = "gateway_rotuer_%d";
    
    /**
     * 7天过期时间
     * 对应Go项目中的TTL7D常量
     */
    public static final Duration TTL_7D = Duration.ofDays(7);
    
    /**
     * Lua脚本名称: 比较并递增客户端ID
     * 对应Go项目中的LuaCompareAndIncrClientID常量
     */
    public static final String LUA_COMPARE_AND_INCR_CLIENT_ID = "LuaCompareAndIncrClientID";
    
    /**
     * 比较并递增客户端ID的Lua脚本
     * 对应Go项目中lua.go文件中的脚本内容
     */
    public static final String LUA_COMPARE_AND_INCR_CLIENT_ID_SCRIPT = 
            "if redis.call('exists', KEYS[1]) == 0 then redis.call('set', KEYS[1], 0) end;" +
            "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('incr', KEYS[1]);" +
            "redis.call('expire', KEYS[1], ARGV[2]); return 1 else return -1 end";
    
    // 私有构造方法防止实例化
    private CacheConstants() {
        throw new IllegalStateException("常量类不应该被实例化");
    }
} 