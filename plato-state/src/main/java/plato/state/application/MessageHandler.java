package plato.state.application;

import plato.common.message.MessageOuterClass.MsgCmd;
import plato.state.rpc.service.CommandContext;

/**
 * 消息处理器接口
 * 用于处理各种类型的消息
 * 对应Go版本中的各种消息处理函数
 */
public interface MessageHandler {

    /**
     * 处理登录消息
     * 对应Go版本中的loginMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    void handleLoginMsg(CommandContext cmdCtx, MsgCmd msgCmd);

    /**
     * 处理心跳消息
     * 对应Go版本中的hearbeatMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    void handleHeartbeatMsg(CommandContext cmdCtx, MsgCmd msgCmd);

    /**
     * 处理重连消息
     * 对应Go版本中的reConnMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    void handleReConnMsg(CommandContext cmdCtx, MsgCmd msgCmd);

    /**
     * 处理上行消息
     * 对应Go版本中的upMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    void handleUpMsg(CommandContext cmdCtx, MsgCmd msgCmd);

    /**
     * 处理确认消息
     * 对应Go版本中的ackMsgHandler函数
     *
     * @param cmdCtx 命令上下文
     * @param msgCmd 消息命令
     */
    void handleAckMsg(CommandContext cmdCtx, MsgCmd msgCmd);
} 