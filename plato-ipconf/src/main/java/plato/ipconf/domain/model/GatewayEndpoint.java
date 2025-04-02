package plato.ipconf.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关端点
 * 表示一个网关服务实例的连接信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayEndpoint {
    /**
     * 服务ID
     */
    private String serviceId;
    
    /**
     * 主机名或IP地址
     */
    private String host;
    
    /**
     * 端口
     */
    private int port;
    
    /**
     * 权重
     */
    private int weight;
    
    /**
     * 当前连接数
     */
    private int connectionCount;
    
    /**
     * 地理位置信息
     */
    private GeoLocation geoLocation;
    
    /**
     * 健康状态
     */
    private boolean healthy;
    
    /**
     * 获取端点地址
     * 格式：host:port
     * 
     * @return 端点地址
     */
    public String getEndpoint() {
        return host + ":" + port;
    }
} 