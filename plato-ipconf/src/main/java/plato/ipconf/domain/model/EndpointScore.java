package plato.ipconf.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 端点得分
 * 表示网关端点的评分信息
 * 对应Go版本中的Endport结构体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointScore implements Comparable<EndpointScore> {
    /**
     * 网关端点
     */
    private GatewayEndpoint endpoint;
    
    /**
     * 动态活跃度分数
     * 基于节点的实时负载情况，如连接数
     * 对应Go版本中的ActiveSorce
     */
    private double activeScore;
    
    /**
     * 静态资源分数
     * 基于节点的地理位置和网络质量
     * 对应Go版本中的StaticSorce
     */
    private double staticScore;
    
    /**
     * 比较方法
     * 首先比较动态活跃度分数，如果相同，则比较静态资源分数
     * 
     * @param other 另一个端点得分
     * @return 比较结果
     */
    @Override
    public int compareTo(EndpointScore other) {
        // 首先比较动态活跃度分数
        if (this.activeScore != other.activeScore) {
            return Double.compare(other.activeScore, this.activeScore); // 降序排列
        }
        
        // 如果动态分数相同，则比较静态资源分数
        return Double.compare(other.staticScore, this.staticScore); // 降序排列
    }
} 