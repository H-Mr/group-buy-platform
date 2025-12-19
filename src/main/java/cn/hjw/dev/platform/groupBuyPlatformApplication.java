package cn.hjw.dev.platform;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication // 标记为Spring Boot应用
@Configurable // 标记为配置类
@EnableAsync // 开启异步任务
@EnableScheduling //开启定时任务
public class groupBuyPlatformApplication {
    public static void main(String[] args) {

        // 1. 先判断环境，提前加载.env（仅dev环境）
        // 先通过临时方式获取激活的环境（不启动Spring上下文）
        String activeEnv = System.getProperty("spring.profiles.active", "dev");
        if ("dev".equals(activeEnv)) {
            // 把dotenv加载逻辑抽成独立方法，避免prod环境加载该类
            loadDevEnv();
        }

        // 2. 再启动Spring应用（此时配置文件解析时已能读到变量）
        SpringApplication.run(groupBuyPlatformApplication.class, args);



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