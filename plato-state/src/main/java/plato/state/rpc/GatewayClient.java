package plato.state.rpc;

/**
 * 网关客户端接口
 * 用于与网关服务通信
 * 对应Go版本中的client/gateway.go文件
 */
public interface GatewayClient {

    /**
     * 删除连接
     * 对应Go版本中的DelConn函数
     *
     * @param connId  连接ID
     * @param payload 负载数据
     * @return 是否成功
     */
    boolean delConn(long connId, byte[] payload);

    /**
     * 推送消息
     * 对应Go版本中的Push函数
     *
     * @param connId  连接ID
     * @param payload 负载数据
     * @return 是否成功
     */
    boolean push(long connId, byte[] payload);
} 