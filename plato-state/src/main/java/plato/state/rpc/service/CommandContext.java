package plato.state.rpc.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 命令上下文
 * 表示一个命令
 * 对应Go版本中的CmdContext结构体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandContext {

    /**
     * 取消连接命令
     * 对应Go版本中的CancelConnCmd常量
     */
    public static final int CANCEL_CONN_CMD = 1;

    /**
     * 发送消息命令
     * 对应Go版本中的SendMsgCmd常量
     */
    public static final int SEND_MSG_CMD = 2;

    /**
     * 命令类型
     * 对应Go版本中的Cmd字段
     */
    private int cmd;

    /**
     * 端点地址
     * 对应Go版本中的Endpoint字段
     */
    private String endpoint;

    /**
     * 连接ID
     * 对应Go版本中的ConnID字段
     */
    private long connId;

    /**
     * 负载数据
     * 对应Go版本中的Payload字段
     */
    private byte[] payload;
} 