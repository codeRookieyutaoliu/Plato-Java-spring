package plato.gateway.infrastructure.handler;

import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import plato.common.handler.MessageHandler;
import plato.common.message.CmdType;
import plato.gateway.infrastructure.connection.ConnectionTable;

/**
 * 抽象消息处理器
 * 提供基础的消息处理功能
 */
@Slf4j
public abstract class AbstractMessageHandler<T extends Message> implements MessageHandler<T> {
    
    // 连接表
    protected final ConnectionTable connectionTable;
    
    /**
     * 构造函数
     *
     * @param connectionTable 连接表
     */
    protected AbstractMessageHandler(ConnectionTable connectionTable) {
        this.connectionTable = connectionTable;
    }
    
    /**
     * 获取处理的消息类型
     *
     * @return 消息类型
     */
    @Override
    public abstract CmdType getType();
    
    /**
     * 获取处理的消息类
     *
     * @return 消息类
     */
    @Override
    public abstract Class<T> getMessageClass();
    
    /**
     * 处理消息
     *
     * @param connectionId 连接ID
     * @param message 消息对象
     * @return 处理结果
     */
    @Override
    public boolean handle(long connectionId, T message) {
        try {
            log.debug("处理消息: type={}, connectionId={}", getType(), connectionId);
            return doHandle(connectionId, message);
        } catch (Exception e) {
            log.error("处理消息失败: type={}, connectionId={}, error={}", getType(), connectionId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 具体的消息处理逻辑
     *
     * @param connectionId 连接ID
     * @param message 消息对象
     * @return 处理结果
     */
    protected abstract boolean doHandle(long connectionId, T message);
} 