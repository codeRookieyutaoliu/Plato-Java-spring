package plato.gateway.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import plato.gateway.application.service.ConnectionService;

import java.util.Map;

/**
 * 连接控制器
 * 提供连接管理的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/api/connections")
public class ConnectionController {
    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * 获取连接统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getConnectionStats() {
        return ResponseEntity.ok(connectionService.getConnectionStats());
    }

    /**
     * 关闭指定连接
     *
     * @param connectionId 连接ID
     * @return 操作结果
     */
    @DeleteMapping("/{connectionId}")
    public ResponseEntity<Void> closeConnection(@PathVariable long connectionId) {
        connectionService.closeConnection(connectionId);
        return ResponseEntity.ok().build();
    }

    /**
     * 清理空闲连接
     *
     * @param maxIdleTimeSeconds 最大空闲时间（秒）
     * @return 清理的连接数量
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Integer> cleanupIdleConnections(
            @RequestParam(defaultValue = "300") int maxIdleTimeSeconds) {
        int count = connectionService.cleanupIdleConnections(maxIdleTimeSeconds);
        return ResponseEntity.ok(count);
    }
} 