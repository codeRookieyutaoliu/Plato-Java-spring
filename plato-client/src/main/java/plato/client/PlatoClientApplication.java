package plato.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import plato.client.interfaces.cmd.ClientCommand;
import plato.client.interfaces.gui.JavaFxUI;
import plato.common.sdk.ChatClient;

/**
 * Plato客户端应用程序
 * 应用程序入口
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PlatoClientApplication {
    /**
     * 主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 检查命令行参数，优先使用命令行参数指定的模式
        if (containsOption(args, "--gui") || containsOption(args, "-g")) {
            // GUI模式
            runGuiMode(args);
        } else if (containsOption(args, "--picocli") || containsOption(args, "-p")) {
            // Picocli模式
            runPicocliMode(args);
        } else {
            // Spring Shell模式（默认）
            runShellMode(args);
        }
    }
    
    /**
     * 运行GUI模式
     * 
     * @param args 命令行参数
     */
    private static void runGuiMode(String[] args) {
        // 设置模式属性
        System.setProperty("plato.client.mode", "gui");
        
        // 启动Spring应用程序
        ConfigurableApplicationContext context = SpringApplication.run(PlatoClientApplication.class, args);
        
        // 获取ChatClient bean
        ChatClient chatClient = context.getBean(ChatClient.class);
        
        // 设置ChatClient并启动JavaFX
        JavaFxUI.setClient(chatClient);
        JavaFxUI.launch(args);
    }
    
    /**
     * 运行Picocli模式
     * 
     * @param args 命令行参数
     */
    private static void runPicocliMode(String[] args) {
        ClientCommand.main(args);
    }
    
    /**
     * 运行Shell模式
     * 
     * @param args 命令行参数
     */
    private static void runShellMode(String[] args) {
        // 设置模式属性
        System.setProperty("plato.client.mode", "shell");
        
        // 启动Spring应用程序
        SpringApplication.run(PlatoClientApplication.class, args);
    }
    
    /**
     * 检查参数中是否包含指定选项
     *
     * @param args   参数数组
     * @param option 选项名称
     * @return 是否包含选项
     */
    private static boolean containsOption(String[] args, String option) {
        for (String arg : args) {
            if (arg.equals(option)) {
                return true;
            }
        }
        return false;
    }
} 