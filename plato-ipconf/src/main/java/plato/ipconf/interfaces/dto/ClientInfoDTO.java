package plato.ipconf.interfaces.dto;

import lombok.Data;

/**
 * 客户端信息DTO
 * 用于接收客户端请求中的信息
 */
@Data
public class ClientInfoDTO {
    
    /**
     * 客户端IP地址
     */
    private String ip;
    
    /**
     * 客户端类型（如Android、iOS、Web等）
     */
    private String clientType;
    
    /**
     * 客户端版本
     */
    private String clientVersion;
    
    /**
     * 网络类型（如WiFi、4G、5G等）
     */
    private String networkType;
    
    /**
     * 认证令牌
     */
    private String token;
    
    /**
     * 返回的端点数量限制，默认为3
     */
    private int limit = 3;
} 