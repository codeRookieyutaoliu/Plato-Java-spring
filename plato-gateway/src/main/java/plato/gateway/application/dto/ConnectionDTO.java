package plato.gateway.application.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

/**
 * 连接数据传输对象
 * 用于在应用层之间传递连接信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionDTO {
    /**
     * 连接ID
     */
    private long id;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 客户端端口
     */
    private int clientPort;

    /**
     * 创建时间
     */
    private Instant createTime;

    /**
     * 最后活动时间
     */
    private Instant lastActiveTime;

    /**
     * 是否活跃
     */
    private boolean active;

    /**
     * 连接地址信息
     */
    private String addressInfo;
} 