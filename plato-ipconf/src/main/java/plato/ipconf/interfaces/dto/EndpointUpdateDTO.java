package plato.ipconf.interfaces.dto;

import lombok.Data;

/**
 * 端点更新DTO
 * 用于接收端点连接数更新请求
 */
@Data
public class EndpointUpdateDTO {
    
    /**
     * 端点地址（格式：host:port）
     */
    private String endpoint;
    
    /**
     * 当前连接数
     */
    private int connectionCount;
} 