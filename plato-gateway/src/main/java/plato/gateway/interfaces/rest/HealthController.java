package plato.gateway.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plato.gateway.application.service.ConnectionService;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供系统健康状态检查接口
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final ConnectionService connectionService;

    public HealthController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * 获取系统健康状态
     *
     * @return 健康状态信息
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("activeConnections", connectionService.getConnectionStats().get("activeConnections"));
        return ResponseEntity.ok(status);
    }
} 