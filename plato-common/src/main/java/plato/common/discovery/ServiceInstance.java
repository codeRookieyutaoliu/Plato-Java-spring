package plato.common.discovery;

import java.util.Map;
import java.util.HashMap;

/**
 * 服务实例信息
 * 用于表示一个服务实例的基本信息，包括主机、端口、元数据等
 */
public class ServiceInstance {
    private String host;
    private int port;
    private Map<String, String> metadata;

    public ServiceInstance(String host, int port) {
        this.host = host;
        this.port = port;
        this.metadata = new HashMap<>();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return this.metadata.get(key);
    }
}