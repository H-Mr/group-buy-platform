package cn.hjw.dev.platform.app.config;

import io.github.cdimascio.dotenv.Dotenv;

public class DevEnvLoader {

    public static void loadDevEnv() {
        try {
            // 开发环境加载.env，缺失则报错
            Dotenv dotenv = Dotenv.load();
            // 注入到系统属性，供Spring读取
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            throw new RuntimeException("开发环境缺少.env文件，请检查！", e);
        }
    }
}
