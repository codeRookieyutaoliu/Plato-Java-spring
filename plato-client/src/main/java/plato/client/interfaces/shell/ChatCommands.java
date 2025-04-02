package plato.client.interfaces.shell;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import plato.common.sdk.ChatClient;
import plato.common.sdk.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 聊天命令
 * 使用Spring Shell实现命令行交互
 */
@ShellComponent
@RequiredArgsConstructor
@Slf4j
public class ChatCommands {
    private final ChatClient chatClient;
    private final List<MessageRecord> messageHistory = new ArrayList<>();
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /**
     * 初始化方法
     * 在Spring容器启动后自动调用
     */
    @PostConstruct
    public void init() {
        // 设置消息接收处理
        chatClient.receive(this::handleMessage);
        
        // 连接到服务器
        boolean success = chatClient.connect();
        if (success) {
            connected.set(true);
            log.info("已连接到服务器");
        } else {
            log.error("连接服务器失败");
        }
    }

    /**
     * 发送消息命令
     *
     * @param content 消息内容
     * @return 命令执行结果
     */
    @ShellMethod(value = "发送消息", key = "send")
    @ShellMethodAvailability("isConnected")
    public String send(@ShellOption(help = "消息内容") String content) {
        if (content == null || content.trim().isEmpty()) {
            return "消息内容不能为空";
        }

        // 创建消息
        Message message = Message.builder()
                .type(Message.TYPE_TEXT)
                .content(content)
                .build();

        // 发送消息
        boolean success = chatClient.send(message);
        if (success) {
            // 添加自己的消息到历史记录
            messageHistory.add(new MessageRecord("我", content));
            return "消息已发送";
        } else {
            return "发送消息失败";
        }
    }

    /**
     * 显示消息历史命令
     *
     * @param count 显示的消息数量
     * @return 消息历史
     */
    @ShellMethod(value = "显示消息历史", key = "history")
    public String history(@ShellOption(help = "显示的消息数量", defaultValue = "10") int count) {
        if (messageHistory.isEmpty()) {
            return "暂无消息历史";
        }

        int startIndex = Math.max(0, messageHistory.size() - count);
        List<MessageRecord> messages = messageHistory.subList(startIndex, messageHistory.size());

        // 创建表格模型
        String[][] data = new String[messages.size() + 1][];
        data[0] = new String[]{"发送者", "消息内容"};
        
        for (int i = 0; i < messages.size(); i++) {
            MessageRecord record = messages.get(i);
            data[i + 1] = new String[]{record.sender(), record.content()};
        }

        ArrayTableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        tableBuilder.addFullBorder(BorderStyle.fancy_light);
        
        return tableBuilder.build().render(80);
    }

    /**
     * 重新连接命令
     *
     * @return 命令执行结果
     */
    @ShellMethod(value = "重新连接到服务器", key = "reconnect")
    public String reconnect() {
        boolean success = chatClient.reconnect();
        if (success) {
            connected.set(true);
            return "重新连接成功";
        } else {
            connected.set(false);
            return "重新连接失败";
        }
    }

    /**
     * 显示状态命令
     *
     * @return 客户端状态
     */
    @ShellMethod(value = "显示客户端状态", key = "status")
    public String status() {
        StringBuilder sb = new StringBuilder();
        sb.append("客户端状态:\n");
        sb.append("- 连接状态: ").append(chatClient.isConnected() ? "已连接" : "未连接").append("\n");
//        sb.append("- 用户昵称: ").append(chatClient.getNickname()).append("\n");
//        sb.append("- 用户ID: ").append(chatClient.getUserId()).append("\n");
//        sb.append("- 会话ID: ").append(chatClient.getSessionId()).append("\n");
        sb.append("- 连接ID: ").append(chatClient.getConnectionId()).append("\n");
        sb.append("- 消息数量: ").append(messageHistory.size());
        
        return sb.toString();
    }

    /**
     * 清空消息历史命令
     *
     * @return 命令执行结果
     */
    @ShellMethod(value = "清空消息历史", key = "clear")
    public String clear() {
        messageHistory.clear();
        return "消息历史已清空";
    }

    /**
     * 退出命令
     *
     * @return 命令执行结果
     */
    @ShellMethod(value = "退出客户端", key = "exit")
    public String exit() {
        chatClient.close();
        connected.set(false);
        return "客户端已关闭，请输入 'quit' 退出Shell";
    }

    /**
     * 检查命令是否可用
     *
     * @return 命令可用性
     */
    public Availability isConnected() {
        return chatClient.isConnected()
                ? Availability.available()
                : Availability.unavailable("未连接到服务器，请先使用 'reconnect' 命令连接");
    }

    /**
     * 处理接收到的消息
     *
     * @param message 接收到的消息
     */
    private void handleMessage(Message message) {
        if (message == null) {
            return;
        }

        String sender = message.getName();
        if (sender == null || sender.isEmpty()) {
            sender = "系统";
        }

        String content = message.getContent();
        if (content == null || content.isEmpty()) {
            content = "(空消息)";
        }

        // 添加消息到历史记录
        messageHistory.add(new MessageRecord(sender, content));
        
        // 打印消息
        log.info("收到消息 - {}: {}", sender, content);
    }

    /**
     * 消息记录
     *
     * @param sender  发送者
     * @param content 消息内容
     */
    private record MessageRecord(String sender, String content) {
    }
} 