package cn.hjw.dev.platform;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication // 标记为Spring Boot应用
@Configurable // 标记为配置类
@EnableAsync // 开启异步任务
@EnableScheduling //开启定时任务
public class groupBuyPlatformApplication {
    public static void main(String[] args) {
        // 1. 先启动Spring上下文（获取环境变量），再判断是否加载dotenv
        SpringApplication app = new SpringApplication(groupBuyPlatformApplication.class);
        Environment env = app.run(args).getEnvironment();

        // 2. 仅dev环境加载dotenv（prod环境完全不碰dotenv类）
        String activeEnv = env.getProperty("spring.profiles.active", "dev");
        if ("dev".equals(activeEnv)) {
            // 把dotenv加载逻辑抽成独立方法，避免prod环境加载该类
            loadDevEnv();
        }
    }

    // 该方法仅dev环境调用，prod环境不会执行，避免加载Dotenv类
    private static void loadDevEnv() {
        try {
            // 开发环境加载.env，缺失则报错
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
            // 注入到系统属性，供Spring读取
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            throw new RuntimeException("开发环境缺少.env文件，请检查！", e);
        }
    }
}