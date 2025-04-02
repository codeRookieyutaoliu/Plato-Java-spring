package plato.cache.infrastructure.redis;

import lombok.extern.slf4j.Slf4j;
import plato.cache.domain.valueobject.CacheConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lua脚本注册表
 * 负责管理和缓存Redis Lua脚本
 * 对应Go项目中的lua.go文件中的luaScriptTable
 */
@Slf4j
public class LuaScriptRegistry {

    /**
     * 脚本注册表
     * 键为脚本名称，值为脚本内容
     */
    private static final Map<String, String> SCRIPT_REGISTRY = new ConcurrentHashMap<>();
    
    /**
     * SHA1缓存
     * 键为脚本内容，值为SHA1
     */
    private static final Map<String, String> SHA1_CACHE = new ConcurrentHashMap<>();
    
    static {
        // 初始化脚本
        SCRIPT_REGISTRY.put(
                CacheConstants.LUA_COMPARE_AND_INCR_CLIENT_ID,
                CacheConstants.LUA_COMPARE_AND_INCR_CLIENT_ID_SCRIPT
        );
        
        log.info("初始化Lua脚本注册表，已注册{}个脚本", SCRIPT_REGISTRY.size());
    }
    
    /**
     * 注册脚本
     *
     * @param name 脚本名称
     * @param script 脚本内容
     */
    public static void registerScript(String name, String script) {
        SCRIPT_REGISTRY.put(name, script);
        log.info("注册Lua脚本：{}", name);
    }
    
    /**
     * 获取脚本
     *
     * @param name 脚本名称
     * @return 脚本内容
     */
    public static String getScript(String name) {
        String script = SCRIPT_REGISTRY.get(name);
        if (script == null) {
            throw new IllegalArgumentException("未注册的Lua脚本：" + name);
        }
        return script;
    }
    
    /**
     * 计算脚本的SHA1值
     * Redis使用SHA1验证脚本的完整性
     *
     * @param script 脚本内容
     * @return SHA1值
     */
    public static String getSha1(String script) {
        return SHA1_CACHE.computeIfAbsent(script, LuaScriptRegistry::calculateSha1);
    }
    
    /**
     * 计算SHA1值
     *
     * @param script 脚本内容
     * @return SHA1值
     */
    private static String calculateSha1(String script) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(script.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("计算SHA1失败", e);
            throw new RuntimeException("计算SHA1失败", e);
        }
    }
    
    // 私有构造函数防止实例化
    private LuaScriptRegistry() {
        throw new IllegalStateException("工具类不应该被实例化");
    }
} 