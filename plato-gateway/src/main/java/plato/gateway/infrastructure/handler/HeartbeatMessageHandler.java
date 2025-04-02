package plato.gateway.infrastructure.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plato.common.codec.MessageCodec;
import plato.common.message.AckMsg;
import plato.common.message.CmdType;
import plato.common.message.HeartbeatMsg;
import plato.common.message.MsgCmd;
import plato.gateway.infrastructure.connection.IConnection;
import plato.gateway.infrastructure.connection.ConnectionTable;

/**
 * 心跳消息处理器
 * 处理心跳消息
 */
@Slf4j
@Component
public class HeartbeatMessageHandler extends AbstractMessageHandler<HeartbeatMsg> {
    
    // 消息编解码器
    private final MessageCodec messageCodec;
    
    /**
     * 构造函数
     *
     * @param connectionTable 连接表
     * @param messageCodec 消息编解码器
     */
    @Autowired
    public HeartbeatMessageHandler(ConnectionTable connectionTable, MessageCodec messageCodec) {
        super(connectionTable);
        this.messageCodec = messageCodec;
    }
    
    /**
     * 获取处理的消息类型
     *
     * @return 消息类型
     */
    @Override
    public CmdType getType() {
        return CmdType.HEARTBEAT;
    }
    
    /**
     * 获取处理的消息类
     *
     * @return 消息类
     */
    @Override
    public Class<HeartbeatMsg> getMessageClass() {
        return HeartbeatMsg.class;
    }
    
    /**
     * 处理心跳消息
     *
     * @param connectionId 连接ID
     * @param message 心跳消息
     * @return 处理结果
     */
    @Override
    protected boolean doHandle(long connectionId, HeartbeatMsg message) {
        log.debug("处理心跳消息: connectionId={}", connectionId);
        
        // 获取连接
        IConnection connection = connectionTable.get(connectionId);
        if (connection == null) {
            log.warn("连接不存在: connectionId={}", connectionId);
            return false;
        }
        
        // 更新最后活动时间
        connection.updateLastActiveTime();
        
        // 发送 ACK 消息
        AckMsg ackMsg = AckMsg.newBuilder()
                .setCode(0)
                .setMsg("OK")
                .setType(CmdType.HEARTBEAT)
                .setConnId(connectionId)
                .build();
        
        // 封装为顶层消息
        MsgCmd msgCmd = MsgCmd.newBuilder()
                .setType(CmdType.ACK)
                .setPayload(ackMsg.toByteString())
                .build();
        
        // 编码消息
        byte[] data = messageCodec.encode(msgCmd);
        
        // 发送消息
        return connection.sendMessage(data);
    }
} 