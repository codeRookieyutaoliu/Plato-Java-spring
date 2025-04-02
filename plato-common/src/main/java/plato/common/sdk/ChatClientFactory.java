package plato.common.sdk;

import java.net.InetAddress;

/**
 * 聊天客户端工厂接口
 * 用于创建ChatClient实例
 */
public interface ChatClientFactory {
    /**
     * 创建聊天客户端
     * 对应Go代码中的NewChat函数
     * 
     * @param ip 服务器IP地址
     * @param port 服务器端口
     * @param nickname 用户昵称
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 聊天客户端实例
     */
    ChatClient createChatClient(InetAddress ip, int port, String nickname, String userId, String sessionId);
} 