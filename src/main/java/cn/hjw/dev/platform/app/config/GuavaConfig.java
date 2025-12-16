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

    // 注入 EventBus 专用的小池子
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

    /**
     * DCC 事件总线，使用单线程池，彻底消除并发写同一 Key 的竞态问题
     */
    @Bean("dcc-event-bus")
    public EventBus dccEventBus() {
        // 使用单线程池，彻底消除并发写同一 Key 的竞态问题
        ExecutorService eventExecutor = Executors.newSingleThreadExecutor(
        r -> {
            Thread t = new Thread(r, "dcc-event-worker");
            t.setDaemon(true);
            return t;
        });
        // 使用 AsyncEventBus
        return new AsyncEventBus("dcc-event-bus",eventExecutor);
    }

}
