package plato.client.interfaces.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import plato.common.sdk.ChatClient;
import plato.common.sdk.Message;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JavaFX聊天界面
 * 提供图形化的聊天界面
 */
@Slf4j
public class JavaFxUI extends Application {
    @Setter
    private static ChatClient chatClient;
    private TextArea messageArea;
    private TextField inputField;
    private Button sendButton;
    private Button reconnectButton;
    private Label statusLabel;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /**
     * 设置聊天客户端
     * 
     * @param client 聊天客户端
     */
    public static void setClient(ChatClient client) {
        chatClient = client;
    }

    @Override
    public void start(Stage primaryStage) {
        // 创建主布局
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f0f0f0;");

        // 创建状态栏
        HBox statusBar = createStatusBar();
        
        // 创建消息区域
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");
        VBox.setVgrow(messageArea, Priority.ALWAYS);

        // 创建输入区域
        HBox inputArea = createInputArea();

        // 添加组件到主布局
        root.getChildren().addAll(statusBar, messageArea, inputArea);

        // 创建场景
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Plato 聊天客户端");

        // 设置窗口关闭事件
        primaryStage.setOnCloseRequest(event -> {
            if (chatClient != null) {
                chatClient.close();
            }
            Platform.exit();
        });

        // 初始化客户端
        initializeClient();

        // 显示窗口
        primaryStage.show();
    }

    /**
     * 创建状态栏
     *
     * @return 状态栏布局
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #d0d0d0; -fx-border-width: 0 0 1 0;");

        statusLabel = new Label("未连接");
        statusLabel.setStyle("-fx-text-fill: #ff0000;");

        reconnectButton = new Button("重新连接");
        reconnectButton.setOnAction(e -> reconnect());
        reconnectButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        statusBar.getChildren().addAll(new Label("状态:"), statusLabel, reconnectButton);
        return statusBar;
    }

    /**
     * 创建输入区域
     *
     * @return 输入区域布局
     */
    private HBox createInputArea() {
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);

        inputField = new TextField();
        inputField.setPromptText("输入消息...");
        inputField.setOnAction(e -> sendMessage());
        HBox.setHgrow(inputField, Priority.ALWAYS);

        sendButton = new Button("发送");
        sendButton.setOnAction(e -> sendMessage());
        sendButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        sendButton.setDisable(true);

        inputArea.getChildren().addAll(inputField, sendButton);
        return inputArea;
    }

    /**
     * 初始化聊天客户端
     */
    private void initializeClient() {
        if (chatClient == null) {
            log.error("ChatClient未初始化");
            return;
        }

        // 设置消息接收处理
        chatClient.receive(this::handleMessage);

        // 连接到服务器
        boolean success = chatClient.connect();
        updateConnectionStatus(success);
    }

    /**
     * 重新连接到服务器
     */
    private void reconnect() {
        if (chatClient == null) {
            return;
        }

        reconnectButton.setDisable(true);
        boolean success = chatClient.reconnect();
        updateConnectionStatus(success);
        reconnectButton.setDisable(false);
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        if (!connected.get() || inputField.getText().trim().isEmpty()) {
            return;
        }

        String content = inputField.getText().trim();
        Message message = Message.builder()
                .type(Message.TYPE_TEXT)
                .content(content)
                .build();

        if (chatClient.send(message)) {
            appendMessage("我", content);
            inputField.clear();
        } else {
            showError("发送消息失败");
        }
    }

    /**
     * 处理接收到的消息
     *
     * @param message 接收到的消息
     */
    private void handleMessage(Message message) {
        if (message == null) {
            log.warn("接收到空消息");
            return;
        }

        // 根据消息类型进行不同的处理
        switch (message.getType()) {
            case Message.TYPE_TEXT:
                handleTextMessage(message);
                break;
            case Message.TYPE_ACK:
                handleAckMessage(message);
                break;
            case Message.TYPE_HEARTBEAT:
                // 心跳消息通常不需要显示
                log.debug("接收到心跳消息");
                break;
            default:
                // 未知类型的消息
                log.warn("接收到未知类型的消息: {}", message.getType());
                String content = message.getContent();
                if (content == null || content.isEmpty()) {
                    content = "(未知消息)";
                }
                final String finalContent = content;
                Platform.runLater(() -> appendMessage("系统", finalContent));
                break;
        }
    }

    /**
     * 处理文本消息
     *
     * @param message 文本消息
     */
    private void handleTextMessage(Message message) {
        String sender = message.getName();
        if (sender == null || sender.isEmpty()) {
            sender = "匿名用户";
        }

        String content = message.getContent();
        if (content == null || content.isEmpty()) {
            content = "(空消息)";
        }

        final String finalSender = sender;
        final String finalContent = content;
        Platform.runLater(() -> appendMessage(finalSender, finalContent));
    }

    /**
     * 处理确认消息
     *
     * @param message 确认消息
     */
    private void handleAckMessage(Message message) {
        String content = message.getContent();
        if (content == null || content.isEmpty()) {
            content = "消息已确认";
        }

        final String finalContent = content;
        Platform.runLater(() -> appendMessage("系统", finalContent));
    }

    /**
     * 添加消息到消息区域
     *
     * @param sender  发送者
     * @param content 消息内容
     */
    private void appendMessage(String sender, String content) {
        String message = String.format("[%s] %s: %s%n",
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                sender,
                content);
        messageArea.appendText(message);
    }

    /**
     * 更新连接状态
     *
     * @param isConnected 是否已连接
     */
    private void updateConnectionStatus(boolean isConnected) {
        Platform.runLater(() -> {
            connected.set(isConnected);
            if (isConnected) {
                statusLabel.setText("已连接");
                statusLabel.setStyle("-fx-text-fill: #00aa00;");
            } else {
                statusLabel.setText("未连接");
                statusLabel.setStyle("-fx-text-fill: #ff0000;");
            }
            sendButton.setDisable(!isConnected);
        });
    }

    /**
     * 显示错误消息
     *
     * @param message 错误消息
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
} 