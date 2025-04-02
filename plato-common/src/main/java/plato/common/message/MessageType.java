package plato.common.message;

/**
 * 消息类型枚举
 * <p>
 * 定义系统支持的所有消息类型
 * 对应Go代码中的CmdType枚举
 * </p>
 */
public enum MessageType {
    
    /**
     * 登录消息
     * 用户登录时发送
     */
    LOGIN(0, "登录消息"),
    
    /**
     * 心跳消息
     * 用于保持连接活跃
     */
    HEARTBEAT(1, "心跳消息"),
    
    /**
     * 重连消息
     * 连接断开后重新连接时发送
     */
    RECONNECT(2, "重连消息"),
    
    /**
     * 确认消息
     * 用于确认消息接收
     */
    ACK(3, "确认消息"),
    
    /**
     * 上行消息
     * 客户端发送到服务器的业务消息
     */
    UPLINK(4, "上行消息"),
    
    /**
     * 推送消息
     * 服务器推送给客户端的业务消息
     */
    PUSH(5, "推送消息"),
    
    /**
     * 文本消息
     * 包含文本内容的消息
     */
    TEXT(6, "文本消息"),
    
    /**
     * 图片消息
     * 包含图片内容的消息
     */
    IMAGE(7, "图片消息"),
    
    /**
     * 语音消息
     * 包含语音内容的消息
     */
    VOICE(8, "语音消息"),
    
    /**
     * 视频消息
     * 包含视频内容的消息
     */
    VIDEO(9, "视频消息"),
    
    /**
     * 位置消息
     * 包含地理位置信息的消息
     */
    LOCATION(10, "位置消息"),
    
    /**
     * 文件消息
     * 包含文件内容的消息
     */
    FILE(11, "文件消息"),
    
    /**
     * 系统消息
     * 系统发送的通知消息
     */
    SYSTEM(12, "系统消息"),
    
    /**
     * 自定义消息
     * 用户自定义类型的消息
     */
    CUSTOM(13, "自定义消息"),
    
    /**
     * 错误消息
     * 包含错误信息的消息
     */
    ERROR(14, "错误消息"),
    
    /**
     * 撤回消息
     * 用于撤回已发送的消息
     */
    RECALL(15, "撤回消息"),
    
    /**
     * 已读消息
     * 标记消息已读状态
     */
    READ(16, "已读消息"),
    
    /**
     * 未知类型
     * 无法识别的消息类型
     */
    UNKNOWN(99, "未知类型");
    
    /**
     * 类型码
     */
    private final int code;
    
    /**
     * 描述
     */
    private final String description;
    
    /**
     * 构造函数
     *
     * @param code 类型码
     * @param description 描述
     */
    MessageType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取类型码
     *
     * @return 类型码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据类型码获取消息类型
     *
     * @param code 类型码
     * @return 消息类型枚举值，如果不存在返回UNKNOWN
     */
    public static MessageType fromCode(int code) {
        for (MessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * 根据名称获取消息类型（忽略大小写）
     *
     * @param name 类型名称
     * @return 消息类型枚举值，如果不存在返回UNKNOWN
     */
    public static MessageType fromName(String name) {
        if (name == null || name.isEmpty()) {
            return UNKNOWN;
        }
        
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
    
    /**
     * 判断是否为业务消息
     *
     * @return 如果是业务消息返回true，否则返回false
     */
    public boolean isBusinessMessage() {
        return this == TEXT || this == IMAGE || this == VOICE ||
               this == VIDEO || this == LOCATION || this == FILE ||
               this == CUSTOM || this == SYSTEM;
    }
    
    /**
     * 判断是否为控制消息
     *
     * @return 如果是控制消息返回true，否则返回false
     */
    public boolean isControlMessage() {
        return this == LOGIN || this == HEARTBEAT ||
               this == RECONNECT || this == ACK;
    }
    
    /**
     * 判断是否为状态消息
     *
     * @return 如果是状态消息返回true，否则返回false
     */
    public boolean isStatusMessage() {
        return this == ERROR || this == RECALL || this == READ;
    }
} 