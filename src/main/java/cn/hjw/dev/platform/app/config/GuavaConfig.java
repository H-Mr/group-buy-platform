package cn.hjw.dev.platform.app.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.*;

@Configuration
public class GuavaConfig {

    @Resource(name = "businessEventExecutor")
    private ExecutorService businessEventExecutor;

    @Bean(name = "cache")
    public Cache<String, String> cache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 系统事件总线，使用独立线程池，避免与业务线程池争抢资源
     */
    @Bean("sys-event-bus")
    public EventBus eventBusListener() {
        // 使用 AsyncEventBus
        return new AsyncEventBus("sys-event-bus",businessEventExecutor);
    }

}
