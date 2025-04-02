package plato.common.message;

/**
 * 消息工厂类
 * <p>
 * 用于创建各种类型的消息实例
 * </p>
 */
public final class MessageFactory {
    
    /**
     * 私有构造函数，防止实例化
     */
    private MessageFactory() {
        throw new UnsupportedOperationException("工具类不支持实例化");
    }
    
    /**
     * 创建文本消息
     *
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 文本内容
     * @return 文本消息实例
     */
    public static TextMessage createTextMessage(String senderId, String receiverId, String content) {
        return new TextMessage(senderId, receiverId, content);
    }
    
    /**
     * 创建心跳消息
     *
     * @param clientId 客户端ID
     * @param connectionId 连接ID
     * @return 心跳消息实例
     */
    public static HeartbeatMessage createHeartbeatMessage(String clientId, String connectionId) {
        return new HeartbeatMessage(clientId, connectionId);
    }
    
    /**
     * 创建登录消息
     *
     * @param username 用户名
     * @param password 密码
     * @param deviceId 设备ID
     * @return 登录消息实例
     */
    public static LoginMessage createLoginMessage(String username, String password, String deviceId) {
        return new LoginMessage(username, password, deviceId);
    }
    
    /**
     * 创建登录消息
     *
     * @param username 用户名
     * @param password 密码
     * @param deviceId 设备ID
     * @param deviceType 设备类型
     * @param clientVersion 客户端版本
     * @return 登录消息实例
     */
    public static LoginMessage createLoginMessage(String username, String password, String deviceId, 
                                               String deviceType, String clientVersion) {
        return new LoginMessage(username, password, deviceId, deviceType, clientVersion);
    }
    
    /**
     * 创建重连消息
     *
     * @param connectionId 连接ID
     * @param clientId 客户端ID
     * @return 重连消息实例
     */
    public static ReconnectMessage createReconnectMessage(String connectionId, String clientId) {
        return new ReconnectMessage(connectionId, clientId);
    }
    
    /**
     * 创建重连消息
     *
     * @param connectionId 连接ID
     * @param sessionId 会话ID
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 重连消息实例
     */
    public static ReconnectMessage createReconnectMessage(String connectionId, String sessionId, 
                                                       String clientId, String userId, String deviceId) {
        return new ReconnectMessage(connectionId, sessionId, clientId, userId, deviceId);
    }
    
    /**
     * 创建确认消息
     *
     * @param originType 原始消息类型
     * @param originMessageId 原始消息ID
     * @return 确认消息实例
     */
    public static AckMessage createAckMessage(MessageType originType, String originMessageId) {
        return new AckMessage(originType, originMessageId);
    }
    
    /**
     * 创建确认消息
     *
     * @param code 确认状态码
     * @param msg 确认消息
     * @param originType 原始消息类型
     * @param originMessageId 原始消息ID
     * @return 确认消息实例
     */
    public static AckMessage createAckMessage(int code, String msg, MessageType originType, String originMessageId) {
        return new AckMessage(code, msg, originType, originMessageId);
    }
    
    /**
     * 创建成功确认消息
     *
     * @param originMessage 原始消息
     * @return 确认消息实例
     */
    public static AckMessage createSuccessAck(Message originMessage) {
        AckMessage ackMessage = new AckMessage(originMessage.getType(), originMessage.getMessageId());
        ackMessage.setSenderId(originMessage.getReceiverId());
        ackMessage.setReceiverId(originMessage.getSenderId());
        return ackMessage;
    }
    
    /**
     * 创建失败确认消息
     *
     * @param originMessage 原始消息
     * @param errorCode 错误码
     * @param errorMsg 错误信息
     * @return 确认消息实例
     */
    public static AckMessage createFailureAck(Message originMessage, int errorCode, String errorMsg) {
        AckMessage ackMessage = new AckMessage(errorCode, errorMsg, originMessage.getType(), originMessage.getMessageId());
        ackMessage.setSenderId(originMessage.getReceiverId());
        ackMessage.setReceiverId(originMessage.getSenderId());
        return ackMessage;
    }
} 