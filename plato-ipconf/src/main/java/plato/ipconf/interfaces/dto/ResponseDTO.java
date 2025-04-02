package plato.ipconf.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应DTO
 * 对应Go版本中的Response结构
 * 
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {
    /**
     * 消息
     */
    private String message;
    
    /**
     * 状态码
     */
    private int code;
    
    /**
     * 数据
     */
    private T data;
} 