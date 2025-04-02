package plato.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Plato配置加载器
 * 用于加载plato.yml配置文件
 */
@Slf4j
@Configuration
public class PlatoConfigLoader {

    private final Environment environment;
    private Map<String, Object> platoConfig;

    @Autowired
    public PlatoConfigLoader(Environment environment) {
        this.environment = environment;
        loadConfig();
    }

    /**
     * 加载配置文件
     */
    private void loadConfig() {
        try {
            ClassPathResource resource = new ClassPathResource("plato.yml");
            try (InputStream inputStream = resource.getInputStream()) {
                Yaml yaml = new Yaml();
                platoConfig = yaml.load(inputStream);
                log.info("加载Plato配置文件成功");
            }
        } catch (IOException e) {
            log.error("加载Plato配置文件失败", e);
            platoConfig = Map.of();
        }
    }

    /**
     * 获取全局配置
     */
    @Bean
    @ConfigurationProperties(prefix = "global")
    public GlobalConfig globalConfig() {
        return new GlobalConfig();
    }

    /**
     * 获取网关配置
     */
    @Bean
    @ConfigurationProperties(prefix = "gateway")
    public GatewayConfig gatewayConfig() {
        return new GatewayConfig();
    }

    /**
     * 获取状态服务配置
     */
    @Bean
    @ConfigurationProperties(prefix = "state")
    public StateConfig stateConfig() {
        return new StateConfig();
    }

    /**
     * 获取IP配置服务配置
     */
    @Bean
    @ConfigurationProperties(prefix = "ip_conf")
    public IpConfConfig ipConfConfig() {
        return new IpConfConfig();
    }

    /**
     * 获取用户域配置
     */
    @Bean
    @ConfigurationProperties(prefix = "user_domain")
    public UserDomainConfig userDomainConfig() {
        return new UserDomainConfig();
    }

    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    public Object getConfig(String key) {
        String[] keys = key.split("\\.");
        Map<String, Object> currentMap = platoConfig;
        
        for (int i = 0; i < keys.length - 1; i++) {
            Object value = currentMap.get(keys[i]);
            if (value instanceof Map) {
                currentMap = (Map<String, Object>) value;
            } else {
                return null;
            }
        }
        
        return currentMap.get(keys[keys.length - 1]);
    }

    /**
     * 全局配置
     */
    public static class GlobalConfig {
        private String env;

        public String getEnv() {
            return env;
        }

        public void setEnv(String env) {
            this.env = env;
        }
    }

    /**
     * 网关配置
     */
    public static class GatewayConfig {
        private String serviceName;
        private String serviceAddr;
        private int tcpMaxNum;
        private int epollChannelNum;
        private int epollNum;
        private int epollWaitQueueSize;
        private int tcpServerPort;
        private int rpcServerPort;
        private int workerPoolNum;
        private int cmdChannelNum;
        private int weight;
        private String stateServerEndpoint;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceAddr() {
            return serviceAddr;
        }

        public void setServiceAddr(String serviceAddr) {
            this.serviceAddr = serviceAddr;
        }

        public int getTcpMaxNum() {
            return tcpMaxNum;
        }

        public void setTcpMaxNum(int tcpMaxNum) {
            this.tcpMaxNum = tcpMaxNum;
        }

        public int getEpollChannelNum() {
            return epollChannelNum;
        }

        public void setEpollChannelNum(int epollChannelNum) {
            this.epollChannelNum = epollChannelNum;
        }

        public int getEpollNum() {
            return epollNum;
        }

        public void setEpollNum(int epollNum) {
            this.epollNum = epollNum;
        }

        public int getEpollWaitQueueSize() {
            return epollWaitQueueSize;
        }

        public void setEpollWaitQueueSize(int epollWaitQueueSize) {
            this.epollWaitQueueSize = epollWaitQueueSize;
        }

        public int getTcpServerPort() {
            return tcpServerPort;
        }

        public void setTcpServerPort(int tcpServerPort) {
            this.tcpServerPort = tcpServerPort;
        }

        public int getRpcServerPort() {
            return rpcServerPort;
        }

        public void setRpcServerPort(int rpcServerPort) {
            this.rpcServerPort = rpcServerPort;
        }

        public int getWorkerPoolNum() {
            return workerPoolNum;
        }

        public void setWorkerPoolNum(int workerPoolNum) {
            this.workerPoolNum = workerPoolNum;
        }

        public int getCmdChannelNum() {
            return cmdChannelNum;
        }

        public void setCmdChannelNum(int cmdChannelNum) {
            this.cmdChannelNum = cmdChannelNum;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public String getStateServerEndpoint() {
            return stateServerEndpoint;
        }

        public void setStateServerEndpoint(String stateServerEndpoint) {
            this.stateServerEndpoint = stateServerEndpoint;
        }
    }

    /**
     * 状态服务配置
     */
    public static class StateConfig {
        private String serviceName;
        private String serviceAddr;
        private int cmdChannelNum;
        private int serverPort;
        private int weight;
        private String connStateSlotRange;
        private String gatewayServerEndpoint;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceAddr() {
            return serviceAddr;
        }

        public void setServiceAddr(String serviceAddr) {
            this.serviceAddr = serviceAddr;
        }

        public int getCmdChannelNum() {
            return cmdChannelNum;
        }

        public void setCmdChannelNum(int cmdChannelNum) {
            this.cmdChannelNum = cmdChannelNum;
        }

        public int getServerPort() {
            return serverPort;
        }

        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public String getConnStateSlotRange() {
            return connStateSlotRange;
        }

        public void setConnStateSlotRange(String connStateSlotRange) {
            this.connStateSlotRange = connStateSlotRange;
        }

        public String getGatewayServerEndpoint() {
            return gatewayServerEndpoint;
        }

        public void setGatewayServerEndpoint(String gatewayServerEndpoint) {
            this.gatewayServerEndpoint = gatewayServerEndpoint;
        }
    }

    /**
     * IP配置服务配置
     */
    public static class IpConfConfig {
        private String servicePath;

        public String getServicePath() {
            return servicePath;
        }

        public void setServicePath(String servicePath) {
            this.servicePath = servicePath;
        }
    }

    /**
     * 用户域配置
     */
    public static class UserDomainConfig {
        private String serviceName;
        private String serviceAddr;
        private int servicePort;
        private int weight;
        private String dbDns;

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceAddr() {
            return serviceAddr;
        }

        public void setServiceAddr(String serviceAddr) {
            this.serviceAddr = serviceAddr;
        }

        public int getServicePort() {
            return servicePort;
        }

        public void setServicePort(int servicePort) {
            this.servicePort = servicePort;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public String getDbDns() {
            return dbDns;
        }

        public void setDbDns(String dbDns) {
            this.dbDns = dbDns;
        }
    }
} 