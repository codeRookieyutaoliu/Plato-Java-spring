package plato.common.router;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路由记录
 * 存储设备与连接的路由信息
 * 对应Go版本中的Record结构体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouterRecord {
    /**
     * 端点地址
     * 对应Go版本中的Endpoint字段
     */
    private String endpoint;
    
    /**
     * 连接ID
     * 对应Go版本中的ConndID字段
     */
    private long connId;
} 