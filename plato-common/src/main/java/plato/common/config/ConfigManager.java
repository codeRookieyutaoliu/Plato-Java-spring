package plato.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private int serverPort;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosServerAddr;

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String nacosConfigAddr;

    @Value("${spring.cloud.nacos.config.file-extension}")
    private String configFileExtension;

    @Value("${spring.cloud.nacos.config.group}")
    private String configGroup;

    @Value("${spring.cloud.nacos.discovery.group}")
    private String discoveryGroup;

    @Value("${spring.cloud.nacos.discovery.namespace}")
    private String namespace;

    public String getApplicationName() {
        return applicationName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getNacosServerAddr() {
        return nacosServerAddr;
    }

    public String getNacosConfigAddr() {
        return nacosConfigAddr;
    }

    public String getConfigFileExtension() {
        return configFileExtension;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public String getDiscoveryGroup() {
        return discoveryGroup;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getServiceInstanceId() {
        return String.format("%s-%s-%d", applicationName, namespace, serverPort);
    }
} 