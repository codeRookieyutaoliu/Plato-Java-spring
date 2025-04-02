package plato.state.application.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plato.common.message.MessageOuterClass;
import plato.state.application.CommandProcessor;
import plato.state.application.MessageHandler;
import plato.state.domain.model.CacheStateManager;
import plato.state.domain.model.MessageStateMachine;
import plato.state.rpc.service.CommandContext;
import plato.state.rpc.service.StateService;

/**
 * 命令处理器实现
 * 处理来自Gateway的命令
 * 对应Go版本中的cmdHandler和msgCmdHandler函数
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommandProcessorImpl implements CommandProcessor {

    /**
     * 消息处理器
     * 用于处理不同类型的消息
     */
    private final MessageHandler messageHandler;

    /**
     * 缓存状态管理器
     * 用于管理连接状态
     */
    private final CacheStateManager cacheStateManager;
    
    /**
     * 消息状态机
     */
    private final MessageStateMachine messageStateMachine;

    /**
     * 处理命令
     * 对应Go版本中的cmdHandler函数
     *
     * @param cmdCtx 命令上下文
     */
    @Override
    public void process(CommandContext cmdCtx) {
        try {
            // 检查当前实例是否负责处理该连接
            if (!cacheStateManager.isResponsibleForConn(cmdCtx.getConnId())) {
                log.debug("Not responsible for connection: connId={}", cmdCtx.getConnId());
                return;
            }
            
            // 根据命令类型处理
            switch (cmdCtx.getCmd()) {
                case CommandContext.CANCEL_CONN_CMD:
                    // 处理取消连接命令
                    log.debug("Processing cancel connection command: endpoint={}, connId={}", 
                            cmdCtx.getEndpoint(), cmdCtx.getConnId());
                    cacheStateManager.connLogOut(cmdCtx.getConnId());
                    break;
                    
                case CommandContext.SEND_MSG_CMD:
                    // 处理发送消息命令
                    processMsgCmd(cmdCtx);
                    break;
                    
                default:
                    log.warn("Unknown command type: {}", cmdCtx.getCmd());
            }
        } catch (Exception e) {
            log.error("Error processing command: cmd={}, connId={}", cmdCtx.getCmd(), cmdCtx.getConnId(), e);
        }
    }

    /**
     * 处理消息命令
     * 对应Go版本中的msgCmdHandler函数
     *
     * @param cmdCtx 命令上下文
     */
    private void processMsgCmd(CommandContext cmdCtx) {
        try {
            // 解析消息命令
            MessageOuterClass.MsgCmd msgCmd = MessageOuterClass.MsgCmd.parseFrom(cmdCtx.getPayload());
            
            // 根据消息类型处理
            switch (msgCmd.getType()) {
                case Login:
                    // 处理登录消息
                    log.debug("Processing login message: connId={}", cmdCtx.getConnId());
                    messageHandler.handleLoginMsg(cmdCtx, msgCmd);
                    break;
                    
                case Heartbeat:
                    // 处理心跳消息
                    log.debug("Processing heartbeat message: connId={}", cmdCtx.getConnId());
                    messageHandler.handleHeartbeatMsg(cmdCtx, msgCmd);
                    break;
                    
                case ReConn:
                    // 处理重连消息
                    log.debug("Processing reconnect message: connId={}", cmdCtx.getConnId());
                    messageHandler.handleReConnMsg(cmdCtx, msgCmd);
                    break;
                    
                case UP:
                    // 处理上行消息
                    log.debug("Processing up message: connId={}", cmdCtx.getConnId());
                    messageHandler.handleUpMsg(cmdCtx, msgCmd);
                    break;
                    
                case ACK:
                    // 处理确认消息
                    log.debug("Processing ack message: connId={}", cmdCtx.getConnId());
                    messageHandler.handleAckMsg(cmdCtx, msgCmd);
                    break;
                    
                default:
                    log.warn("Unknown message type: {}", msgCmd.getType());
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing message command: connId={}", cmdCtx.getConnId(), e);
        } catch (Exception e) {
            log.error("Error processing message command: connId={}", cmdCtx.getConnId(), e);
        }
    }
} 