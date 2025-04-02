package plato.gateway.infrastructure.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {
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

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setServiceAddr(String serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    public void setTcpMaxNum(int tcpMaxNum) {
        this.tcpMaxNum = tcpMaxNum;
    }

    public void setEpollChannelNum(int epollChannelNum) {
        this.epollChannelNum = epollChannelNum;
    }

    public void setEpollNum(int epollNum) {
        this.epollNum = epollNum;
    }

    public void setEpollWaitQueueSize(int epollWaitQueueSize) {
        this.epollWaitQueueSize = epollWaitQueueSize;
    }

    public void setTcpServerPort(int tcpServerPort) {
        this.tcpServerPort = tcpServerPort;
    }

    public void setRpcServerPort(int rpcServerPort) {
        this.rpcServerPort = rpcServerPort;
    }

    public void setWorkerPoolNum(int workerPoolNum) {
        this.workerPoolNum = workerPoolNum;
    }

    public void setCmdChannelNum(int cmdChannelNum) {
        this.cmdChannelNum = cmdChannelNum;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setStateServerEndpoint(String stateServerEndpoint) {
        this.stateServerEndpoint = stateServerEndpoint;
    }
}