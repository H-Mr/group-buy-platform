package cn.hjw.dev.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication // 标记为Spring Boot应用
@EnableAsync // 开启异步任务
@EnableScheduling //开启定时任务
public class GroupBuyPlatformApplication {
    public static void main(String[] args) {
        // 1. 准确获取环境标识（兼容JVM参数和系统环境变量，优先级：JVM参数 > 系统环境变量 > 兜底dev）
        String activeEnv = getActiveEnvironment();

        // 2. 仅dev环境执行.env加载，生产环境完全跳过
        if ("dev".equals(activeEnv)) {
            loadDevEnvSafely();
        }

        // 3. 启动Spring Boot应用
        SpringApplication.run(GroupBuyPlatformApplication.class, args);
    }

    /**
     * 准确获取激活的环境标识
     */
    private static String getActiveEnvironment() {
        // 优先读取JVM系统属性（-Dspring.profiles.active=prod）
        String env = System.getProperty("spring.profiles.active");
        if (env == null || env.trim().isEmpty()) {
            // 其次读取系统环境变量（Docker ENV SPRING_PROFILES_ACTIVE=prod，注意大写下划线命名）
            env = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        // 最终兜底（仅本地开发默认dev，生产环境需显式指定）
        return (env == null || env.trim().isEmpty()) ? "dev" : env.trim();
    }

    /**
     * 安全加载dev环境.env文件（通过反射避免生产环境类加载异常）
     */
    private static void loadDevEnvSafely() {
        try {
            // 反射加载Dotenv相关逻辑，生产环境不触发此方法则不会加载该类
            Class<?> dotenvClass = Class.forName("io.github.cdimascio.dotenv.Dotenv");
            Object dotenv = dotenvClass.getMethod("load").invoke(null);
            // 反射获取entries并注入系统属性
            java.util.Set<?> entries = (java.util.Set<?>) dotenvClass.getMethod("entries").invoke(dotenv);
            for (Object entry : entries) {
                String key = (String) entry.getClass().getMethod("getKey").invoke(entry);
                String value = (String) entry.getClass().getMethod("getValue").invoke(entry);
                System.setProperty(key, value);
            }
        } catch (ClassNotFoundException e) {
            // 生产环境若误触发（理论不会），仅打印警告，不中断应用
            System.err.println("警告：未找到java-dotenv依赖，跳过.env加载（生产环境可忽略此提示）");
        } catch (Exception e) {
            throw new RuntimeException("开发环境缺少.env文件或配置异常，请检查！", e);
        }
    }

}