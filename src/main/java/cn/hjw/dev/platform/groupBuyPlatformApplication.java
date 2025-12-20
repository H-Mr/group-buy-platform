package cn.hjw.dev.platform;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication // 标记为Spring Boot应用
@EnableAsync // 开启异步任务
@EnableScheduling //开启定时任务
public class groupBuyPlatformApplication {
    public static void main(String[] args) {

        // 1. 先判断环境，提前加载.env（仅dev环境）
        // 先通过临时方式获取激活的环境（不启动Spring上下文）
        String activeEnv = System.getProperty("spring.profiles.active", "dev");
        if ("dev".equals(activeEnv)) {
            // 反射调用加载方法，避免生产环境加载Dotenv类
            try {
                Class<?> utilClass = Class.forName("cn.hjw.dev.platform.app.config.DevEnvLoader");
                java.lang.reflect.Method loadMethod = utilClass.getMethod("loadDevEnv");
                loadMethod.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException("开发环境.env加载工具类调用失败", e);
            }
        }

        // 2. 再启动Spring应用（此时配置文件解析时已能读到变量）
        SpringApplication.run(groupBuyPlatformApplication.class, args);



    }

}