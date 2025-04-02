package plato.ipconf.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地理位置
 * 表示一个地理位置的坐标和区域信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocation {
    /**
     * 纬度
     */
    private double latitude;
    
    /**
     * 经度
     */
    private double longitude;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 省/州
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * ISP提供商
     */
    private String isp;
    
    /**
     * 计算两个地理位置之间的距离（单位：公里）
     * 使用Haversine公式计算球面距离
     * 
     * @param other 另一个地理位置
     * @return 距离（公里）
     */
    public double distanceTo(GeoLocation other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }
        
        // 地球半径（公里）
        final double R = 6371.0;
        
        // 将经纬度转换为弧度
        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(other.latitude);
        double lon2 = Math.toRadians(other.longitude);
        
        // Haversine公式
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * 判断两个地理位置是否在同一区域
     * 
     * @param other 另一个地理位置
     * @return 是否在同一区域
     */
    public boolean isSameRegion(GeoLocation other) {
        if (other == null) {
            return false;
        }
        
        // 首先判断国家是否相同
        if (this.country != null && other.country != null && !this.country.equals(other.country)) {
            return false;
        }
        
        // 如果省/州信息可用，判断是否相同
        if (this.province != null && other.province != null && !this.province.equals(other.province)) {
            return false;
        }
        
        // 如果城市信息可用，判断是否相同
        if (this.city != null && other.city != null && !this.city.equals(other.city)) {
            return false;
        }
        
        // 如果ISP信息可用，判断是否相同
        if (this.isp != null && other.isp != null && !this.isp.equals(other.isp)) {
            return false;
        }
        
        return true;
    }
} 