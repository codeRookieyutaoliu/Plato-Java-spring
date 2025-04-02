# Plato Cache 模块

`plato-cache` 模块提供了高性能的缓存服务，支持本地缓存和Redis缓存，并实现了多级缓存策略。本模块对应Go项目中的 `common/cache` 包的功能，并做了Java风格的优化和扩展。

## 主要功能

1. **多级缓存支持**：同时支持本地缓存和Redis缓存，读取时优先从本地缓存获取，未命中则从Redis获取
2. **缓存回写**：从Redis获取的数据会自动回写到本地缓存，提高后续访问性能
3. **批量操作**：支持批量读取、写入和删除缓存，提高性能
4. **自动过期**：支持配置缓存过期时间
5. **Lua脚本支持**：支持在Redis中执行Lua脚本，实现原子操作
6. **键前缀支持**：支持为Redis键添加前缀，实现命名空间隔离

## 对应Go项目中的功能

| Go项目功能 | Java实现 |
| --- | --- |
| MSet / MGet / MDel | 批量设置、获取、删除缓存的核心接口 |
| Local / Remote 模式 | 通过 CacheMode 枚举实现 |
| GetBytes / GetUInt64 | CacheService 中提供相应方法 |
| SetBytes / SetString | CacheService 中提供相应方法 |
| Del | CacheService 中提供相应方法 |
| RunLuaInt | CacheService 中提供 runLuaInt 方法 |
| MaxClientIDKey 等常量 | CacheConstants 中定义 |
| LuaCompareAndIncrClientID 脚本 | LuaScriptRegistry 中注册和管理 |

## 使用方法

### 配置缓存模块

在应用程序中启用和配置缓存模块：

```yaml
plato:
  cache:
    redis:
      enabled: true
      host: localhost
      port: 6379
```

### 注入并使用 CacheService

```java
@Service
public class YourService {
    
    private final CacheService cacheService;
    
    @Autowired
    public YourService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
    
    public void yourMethod() {
        // 设置缓存
        cacheService.setString("key", "value");
        
        // 获取缓存
        String value = cacheService.getString("key");
        
        // 删除缓存
        cacheService.del(Arrays.asList("key"));
        
        // 执行Lua脚本
        int result = cacheService.runLuaInt(
            CacheConstants.LUA_COMPARE_AND_INCR_CLIENT_ID,
            Arrays.asList("key"),
            "0", "60"
        );
    }
}
```

### 自定义缓存配置

如果需要更精细的控制缓存行为，可以直接使用 CacheManager：

```java
@Service
public class YourService {
    
    private final CacheManager cacheManager;
    
    @Autowired
    public YourService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        
        // 添加自定义配置的本地缓存
        cacheManager.addCache(CacheOptions.builder()
            .mode(CacheMode.LOCAL)
            .maxSize(100000)
            .expireTime(Duration.ofHours(2))
            .build());
            
        // 添加自定义配置的Redis缓存
        cacheManager.addCache(CacheOptions.builder()
            .mode(CacheMode.REMOTE)
            .keyPrefix("my-app")
            .expireTime(Duration.ofDays(1))
            .build());
    }
}
```

## 扩展功能

相比Go项目，本模块增加了以下扩展功能：

1. **灵活的缓存配置**：通过CacheOptions类提供更丰富的配置选项
2. **多级缓存策略**：实现了本地缓存和远程缓存的协调工作
3. **缓存回写机制**：远程缓存的数据自动回写到本地缓存
4. **更细粒度的键前缀**：支持为不同的缓存实例配置不同的键前缀
5. **自动装配支持**：集成Spring Boot的自动配置机制

## 实现说明

- 本地缓存基于Caffeine实现，提供高性能的内存缓存
- Redis缓存基于Spring Data Redis实现，提供分布式缓存能力
- 缓存管理器负责协调本地缓存和Redis缓存的工作
- 提供CacheService作为便捷的缓存操作入口
- 支持通过配置文件自定义缓存行为
