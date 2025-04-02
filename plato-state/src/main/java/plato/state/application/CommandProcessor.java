package plato.state.application;

import plato.state.rpc.service.CommandContext;

/**
 * 命令处理器接口
 * 用于处理命令
 * 对应Go版本中的cmdHandler函数
 */
public interface CommandProcessor {

    /**
     * 处理命令
     *
     * @param cmdCtx 命令上下文
     */
    void process(CommandContext cmdCtx);
} 