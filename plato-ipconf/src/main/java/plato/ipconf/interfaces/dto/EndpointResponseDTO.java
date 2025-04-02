package plato.ipconf.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * 端点响应DTO
 * 用于返回网关端点信息给客户端
 */
@Data
public class EndpointResponseDTO {
    
    /**
     * 端点地址列表（格式：host:port）
     */
    private List<String> endpoints;
    
    /**
     * 响应时间戳
     */
    private long timestamp;
    
    /**
     * 缓存生存时间（秒）
     */
    private int ttl;
} 