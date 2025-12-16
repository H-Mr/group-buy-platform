package cn.hjw.dev.platform.app.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description http 框架
 * @create 2025-01-31 09:13
 */
@Configuration
public class OKHttpClientConfig {

    /**
        * OkHttpClient 配置,全局单例
     * @return
     */
    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 连接超时
                .readTimeout(30, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(30, TimeUnit.SECONDS)   // 写入超时
                .callTimeout(90, TimeUnit.SECONDS)    // 整个请求的超时
                .build();
    }

}
