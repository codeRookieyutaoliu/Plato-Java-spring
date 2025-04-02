package plato.gateway.infrastructure.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import plato.common.codec.MessageCodec;
import plato.common.message.AckMsg;
import plato.common.message.CmdType;
import plato.common.message.LoginMsg;
import plato.common.message.MsgCmd;
import plato.gateway.infrastructure.connection.IConnection;
import plato.gateway.infrastructure.connection.ConnectionTable;

/**
 * 登录消息处理器
 * 处理登录消息
 */
@Slf4j
@Component
public class LoginMessageHandler extends AbstractMessageHandler<LoginMsg> {
    
    // 消息编解码器
    private final MessageCodec messageCodec;
    
    /**
     * 构造函数
     *
     * @param connectionTable 连接表
     * @param messageCodec 消息编解码器
     */
    @Autowired
    public LoginMessageHandler(ConnectionTable connectionTable, MessageCodec messageCodec) {
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
        return CmdType.LOGIN;
    }
    
    /**
     * 获取处理的消息类
     *
     * @return 消息类
     */
    @Override
    public Class<LoginMsg> getMessageClass() {
        return LoginMsg.class;
    }
    
    /**
     * 处理登录消息
     *
     * @param connectionId 连接ID
     * @param message 登录消息
     * @return 处理结果
     */
    @Override
    protected boolean doHandle(long connectionId, LoginMsg message) {
        log.info("处理登录消息: connectionId={}, deviceId={}", connectionId, message.getHead().getDeviceId());
        
        // 获取连接
        IConnection connection = connectionTable.get(connectionId);
        if (connection == null) {
            log.warn("连接不存在: connectionId={}", connectionId);
            return false;
        }
        
        // 更新最后活动时间
        connection.updateLastActiveTime();
        
        // TODO: 实现登录逻辑，例如验证设备ID、生成会话ID等
        
        // 发送 ACK 消息
        AckMsg ackMsg = AckMsg.newBuilder()
                .setCode(0)
                .setMsg("Login successful")
                .setType(CmdType.LOGIN)
                .setConnId(connectionId)
                .setClientId(message.getHead().getDeviceId())
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