package plato.ipconf.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 客户端信息
 * 表示一个客户端的相关信息，用于计算最佳网关
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {
    /**
     * 客户端IP地址
     */
    private String ip;
    
    /**
     * 客户端地理位置
     */
    private GeoLocation geoLocation;
    
    /**
     * 客户端类型
     * 例如：Android、iOS、Web等
     */
    private String clientType;
    
    /**
     * 客户端版本
     */
    private String clientVersion;
    
    /**
     * 网络类型
     * 例如：WiFi、4G、5G等
     */
    private String networkType;
    
    /**
     * 认证令牌
     */
    private String token;
} 