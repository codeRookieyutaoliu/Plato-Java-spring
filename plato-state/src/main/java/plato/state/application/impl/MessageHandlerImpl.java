package plato.state.application.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import plato.common.message.MessageOuterClass.*;
import plato.state.application.MessageHandler;
import plato.state.domain.model.CacheStateManager;
import plato.state.domain.model.MessageStateMachine;
import plato.state.domain.model.PushMessage;
import plato.state.rpc.GatewayClient;
import plato.state.rpc.service.CommandContext;

/**
 * 消息处理器实现
 * 实现MessageHandler接口
 * 对应Go版本中的各种消息处理函数
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandlerImpl implements MessageHandler {

    /**
     * 缓存状态管理器
     */
    private final CacheStateManager cacheStateManager;

    /**
     * 网关客户端
     */
    private final GatewayClient gatewayClient;
    
    /**
     * 消息状态机
     */
    private final MessageStateMachine messageStateMachine;

    /**
     * 处理登录消息
     * 对应Go版本中的loginMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    @Override
    public void handleLoginMsg(CommandContext cmdCtx, MsgCmd msgCmd) {
        try {
            // 解析登录消息
            LoginMsg loginMsg = LoginMsg.parseFrom(msgCmd.getPayload());
            
            if (loginMsg.getHead() != null) {
                log.info("Processing login message: deviceId={}, connId={}",
                        loginMsg.getHead().getDeviceID(), cmdCtx.getConnId());
                
                // 执行登录
                boolean success = cacheStateManager.connLogin(
                        loginMsg.getHead().getDeviceID(), cmdCtx.getConnId());
                
                if (success) {
                    // 记录客户端ID
                    if (loginMsg.getHead().getClientID() > 0) {
                        messageStateMachine.recordClientId(
                                cmdCtx.getConnId(), 
                                loginMsg.getHead().getClientID(), 
                                loginMsg.getHead().getSessionId());
                    }
                    
                    // 发送登录成功响应
                    sendAckMsg(CmdType.Login, cmdCtx.getConnId(), 0, 0, "login ok");
                } else {
                    // 发送登录失败响应
                    sendAckMsg(CmdType.Login, cmdCtx.getConnId(), 0, 1, "login failed");
                }
            } else {
                log.warn("Login message has no head: connId={}", cmdCtx.getConnId());
                sendAckMsg(CmdType.Login, cmdCtx.getConnId(), 0, 1, "login failed: no head");
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing login message: connId={}", cmdCtx.getConnId(), e);
            sendAckMsg(CmdType.Login, cmdCtx.getConnId(), 0, 1, "login failed: parse error");
        } catch (Exception e) {
            log.error("Error processing login message: connId={}", cmdCtx.getConnId(), e);
            sendAckMsg(CmdType.Login, cmdCtx.getConnId(), 0, 1, "login failed: " + e.getMessage());
        }
    }

    /**
     * 处理心跳消息
     * 对应Go版本中的hearbeatMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    @Override
    public void handleHeartbeatMsg(CommandContext cmdCtx, MsgCmd msgCmd) {
        try {
            // 解析心跳消息
            HeartbeatMsg heartbeatMsg = HeartbeatMsg.parseFrom(msgCmd.getPayload());
            
            // 重置心跳定时器
            cacheStateManager.resetHeartTimer(cmdCtx.getConnId());
            
            log.debug("Heartbeat received: connId={}", cmdCtx.getConnId());
            
            // 注意：为减少通信量，可以不回复心跳的ACK
        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing heartbeat message: connId={}", cmdCtx.getConnId(), e);
        } catch (Exception e) {
            log.error("Error processing heartbeat message: connId={}", cmdCtx.getConnId(), e);
        }
    }

    /**
     * 处理重连消息
     * 对应Go版本中的reConnMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    @Override
    public void handleReConnMsg(CommandContext cmdCtx, MsgCmd msgCmd) {
        try {
            // 解析重连消息
            ReConnMsg reConnMsg = ReConnMsg.parseFrom(msgCmd.getPayload());
            
            // 重连的消息头中的connID才是上一次断开连接的connID
            if (reConnMsg.getHead() != null) {
                log.info("Processing reconnection: oldConnId={}, newConnId={}",
                        reConnMsg.getHead().getConnID(), cmdCtx.getConnId());
                
                // 获取旧连接的客户端ID
                long oldClientId = messageStateMachine.getClientId(reConnMsg.getHead().getConnID());
                String oldSessionId = messageStateMachine.getSessionId(reConnMsg.getHead().getConnID());
                
                // 执行重连
                boolean success = cacheStateManager.reConn(
                        reConnMsg.getHead().getConnID(), cmdCtx.getConnId());
                
                if (success) {
                    // 如果旧连接有客户端ID记录，则复制到新连接
                    if (oldClientId > 0) {
                        messageStateMachine.recordClientId(cmdCtx.getConnId(), oldClientId, oldSessionId);
                        // 删除旧连接的客户端ID记录
                        messageStateMachine.deleteConnClientId(reConnMsg.getHead().getConnID());
                    }
                    
                    // 发送重连成功响应
                    sendAckMsg(CmdType.ReConn, cmdCtx.getConnId(), 0, 0, "reconnect ok");
                    
                    // 重新推送最后一条消息
                    cacheStateManager.rePush(cmdCtx.getConnId());
                } else {
                    // 发送重连失败响应
                    sendAckMsg(CmdType.ReConn, cmdCtx.getConnId(), 0, 1, "reconnect failed");
                }
            } else {
                log.warn("Reconnect message has no head: connId={}", cmdCtx.getConnId());
                sendAckMsg(CmdType.ReConn, cmdCtx.getConnId(), 0, 1, "reconnect failed: no head");
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing reconnect message: connId={}", cmdCtx.getConnId(), e);
            sendAckMsg(CmdType.ReConn, cmdCtx.getConnId(), 0, 1, "reconnect failed: parse error");
        } catch (Exception e) {
            log.error("Error processing reconnect message: connId={}", cmdCtx.getConnId(), e);
            sendAckMsg(CmdType.ReConn, cmdCtx.getConnId(), 0, 1, "reconnect failed: " + e.getMessage());
        }
    }

    /**
     * 处理上行消息
     * 对应Go版本中的upMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    @Override
    public void handleUpMsg(CommandContext cmdCtx, MsgCmd msgCmd) {
        try {
            // 解析上行消息
            UPMsg upMsg = UPMsg.parseFrom(msgCmd.getPayload());
            
            if (upMsg.getHead() != null) {
                log.debug("Processing up message: connId={}, clientId={}, sessionId={}",
                        cmdCtx.getConnId(), upMsg.getHead().getClientID(), upMsg.getHead().getSessionId());
                
                // 使用消息状态机处理上行消息
                boolean success = messageStateMachine.handleUpMsg(
                        cmdCtx.getConnId(), 
                        upMsg.getHead().getClientID(), 
                        upMsg.getHead().getSessionId(), 
                        upMsg.getUPMsgBody().toByteArray());
                
                if (success) {
                    // 发送确认响应
                    sendAckMsg(CmdType.UP, cmdCtx.getConnId(), upMsg.getHead().getClientID(), 0, "ok");
                } else {
                    log.warn("Failed to handle up message: connId={}, clientId={}", 
                            cmdCtx.getConnId(), upMsg.getHead().getClientID());
                }
            } else {
                log.warn("Up message has no head: connId={}", cmdCtx.getConnId());
            }
        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing up message: connId={}", cmdCtx.getConnId(), e);
        } catch (Exception e) {
            log.error("Error processing up message: connId={}", cmdCtx.getConnId(), e);
        }
    }

    /**
     * 处理确认消息
     * 对应Go版本中的ackMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    @Override
    public void handleAckMsg(CommandContext cmdCtx, MsgCmd msgCmd) {
        try {
            // 解析确认消息
            ACKMsg ackMsg = ACKMsg.parseFrom(msgCmd.getPayload());
            
            log.debug("Processing ACK message: connId={}, sessionId={}, msgId={}",
                    ackMsg.getConnID(), ackMsg.getSessionID(), ackMsg.getMsgID());
            
            // 使用消息状态机确认消息
            messageStateMachine.ackMessage(ackMsg.getConnID(), ackMsg.getSessionID(), ackMsg.getMsgID());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing ACK message: connId={}", cmdCtx.getConnId(), e);
        } catch (Exception e) {
            log.error("Error processing ACK message: connId={}", cmdCtx.getConnId(), e);
        }
    }

    /**
     * 发送确认消息
     * 对应Go版本中的sendACKMsg函数
     *
     * @param ackType  确认类型
     * @param connId   连接ID
     * @param clientId 客户端ID
     * @param code     状态码
     * @param msg      消息
     */
    private void sendAckMsg(CmdType ackType, long connId, long clientId, int code, String msg) {
        try {
            // 构建确认消息
            ACKMsg ackMsg = ACKMsg.newBuilder()
                    .setCode(code)
                    .setMsg(msg)
                    .setConnID(connId)
                    .setType(ackType)
                    .setClientID(clientId)
                    .build();
            
            // 序列化确认消息
            byte[] payload = ackMsg.toByteArray();
            
            // 发送消息
            sendMsg(connId, CmdType.ACK, payload);
        } catch (Exception e) {
            log.error("Error sending ACK message: connId={}, type={}", connId, ackType, e);
        }
    }

    /**
     * 发送消息
     * 对应Go版本中的sendMsg函数
     *
     * @param connId 连接ID
     * @param type   消息类型
     * @param payload 负载数据
     */
    private void sendMsg(long connId, CmdType type, byte[] payload) {
        try {
            // 构建消息命令
            MsgCmd msgCmd = MsgCmd.newBuilder()
                    .setType(type)
                    .setPayload(com.google.protobuf.ByteString.copyFrom(payload))
                    .build();
            
            // 序列化消息命令
            byte[] data = msgCmd.toByteArray();
            
            // 发送消息
            gatewayClient.push(connId, data);
        } catch (Exception e) {
            log.error("Error sending message: connId={}, type={}", connId, type, e);
        }
    }

    /**
     * 推送消息
     * 对应Go版本中的pushMsg函数
     *
     * @param connId    连接ID
     * @param msgId     消息ID
     * @param sessionId 会话ID
     * @param data      消息数据
     */
    private void pushMsg(long connId, long msgId, long sessionId, byte[] data) {
        try {
            // 使用消息状态机处理下行消息
            messageStateMachine.handleDownMsg(connId, msgId, String.valueOf(sessionId), data);
        } catch (Exception e) {
            log.error("Error pushing message: connId={}, msgId={}", connId, msgId, e);
        }
    }
} 