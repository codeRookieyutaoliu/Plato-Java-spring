package plato.client.interfaces.cmd;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import plato.client.interfaces.gui.JavaFxUI;
import plato.client.infrastructure.adapter.ChatClientFactoryImpl;
import plato.common.sdk.ChatClient;
import plato.common.sdk.ChatClientFactory;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * 客户端命令
 * 使用Picocli实现命令行解析
 * 对应Go代码中的client命令
 */
@Command(
    name = "client",
    description = "启动Plato客户端",
    mixinStandardHelpOptions = true,
    version = "1.0"
)
public class ClientCommand implements Callable<Integer> {
    @Option(names = {"-h", "--host"}, description = "服务器主机名或IP地址", defaultValue = "127.0.0.1")
    private String host;

    @Option(names = {"-p", "--port"}, description = "服务器端口", defaultValue = "8900")
    private int port;

    @Option(names = {"-n", "--nickname"}, description = "用户昵称", defaultValue = "user")
    private String nickname;

    @Option(names = {"-u", "--user-id"}, description = "用户ID", defaultValue = "")
    private String userId;

    @Option(names = {"-s", "--session-id"}, description = "会话ID", defaultValue = "")
    private String sessionId;

    @Option(names = {"-g", "--gui"}, description = "使用图形界面模式", defaultValue = "false")
    private boolean guiMode;

    @Override
    public Integer call() throws Exception {
        // 如果未指定用户ID，生成一个随机ID
        if (userId == null || userId.isEmpty()) {
            userId = UUID.randomUUID().toString().replace("-", "");
        }

        // 如果未指定会话ID，使用用户ID作为会话ID
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = userId;
        }

        // 创建客户端
        ChatClientFactory factory = new ChatClientFactoryImpl();
        ChatClient client = factory.createChatClient(
            InetAddress.getByName(host),
            port,
            nickname,
            userId,
            sessionId
        );

        // 连接到服务器
        if (!client.connect()) {
            System.err.println("无法连接到服务器: " + host + ":" + port);
            return 1;
        }

        // 启动UI
        if (guiMode) {
            // 使用JavaFX图形界面
            JavaFxUI.setClient(client);
            JavaFxUI.launch(new String[0]);
        } else {
            // 使用命令行模式
            System.out.println("已连接到服务器: " + host + ":" + port);
            System.out.println("用户: " + nickname);
            System.out.println("输入消息并按回车发送，输入'exit'退出");
            
            // 设置消息接收处理
            client.receive(message -> {
                if (message != null) {
                    String sender = message.getName();
                    if (sender == null || sender.isEmpty()) {
                        sender = "系统";
                    }
                    System.out.println(sender + ": " + message.getContent());
                }
            });
            
            // 读取用户输入
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            String line;
            while (!(line = scanner.nextLine()).equalsIgnoreCase("exit")) {
                if (!line.trim().isEmpty()) {
                    plato.common.sdk.Message message = plato.common.sdk.Message.builder()
                            .type(plato.common.sdk.Message.TYPE_TEXT)
                            .content(line)
                            .build();
                    if (client.send(message)) {
                        System.out.println("我: " + line);
                    } else {
                        System.err.println("发送失败");
                    }
                }
            }
            
            // 关闭客户端
            client.close();
            scanner.close();
        }

        return 0;
    }

    /**
     * 主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new ClientCommand()).execute(args);
        System.exit(exitCode);
    }
} 