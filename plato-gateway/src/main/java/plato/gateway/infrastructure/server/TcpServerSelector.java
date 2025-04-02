package plato.gateway.infrastructure.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * TCP 服务器选择器
 * 用于根据配置选择服务器类型
 */
@Slf4j
@Component
@RefreshScope
public class TcpServerSelector {
    
    // TCP 服务器工厂
    private final TcpServerFactory tcpServerFactory;
    
    // 服务器类型
    @Value("${plato.gateway.server.type:NETTY}")
    private String serverType;
    
    // 当前使用的服务器
    private ITcpServer currentServer;
    
    /**
     * 构造函数
     *
     * @param tcpServerFactory TCP 服务器工厂
     */
    @Autowired
    public TcpServerSelector(TcpServerFactory tcpServerFactory) {
        this.tcpServerFactory = tcpServerFactory;
    }
    
    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        log.info("初始化 TCP 服务器选择器: serverType={}", serverType);
        
        // 根据配置选择服务器类型
        TcpServerType type;
        try {
            type = TcpServerType.valueOf(serverType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("未知的服务器类型: {}, 使用默认类型: NETTY", serverType);
            type = TcpServerType.NETTY;
        }
        
        // 获取服务器
        currentServer = tcpServerFactory.getTcpServer(type);
        
        log.info("使用 {} 服务器", type);
    }
    
    /**
     * 获取当前使用的服务器
     *
     * @return 当前使用的服务器
     */
    public ITcpServer getCurrentServer() {
        return currentServer;
    }
}