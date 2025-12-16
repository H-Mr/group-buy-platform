package cn.hjw.dev.platform.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class ThreadPoolConfig {

    /**
     * 1. 核心业务 DAG 专用线程池
     * 用于执行：锁单、结算、试算等 CPU/IO 密集型任务
     */
    @Bean("dagExecutor")
    public ThreadPoolExecutor dagExecutor(ThreadPoolConfigProperties properties) {
        return buildExecutor(properties.getDag());
    }

    /**
     * 2. 业务事件总线专用线程池
     * 用于执行：Guava AsyncEventBus 的事件分发
     */
    @Bean("businessEventExecutor")
    public ThreadPoolExecutor businessEventExecutor(ThreadPoolConfigProperties properties) {
        return buildExecutor(properties.getNotify());
    }

    /**
     * 3. 异步任务专用线程池
     * 用于执行：耗时的操作，快速脱离事务上下文
     */
    @Bean("asyncTaskExecutor")
    public ThreadPoolExecutor asyncTaskExecutor(ThreadPoolConfigProperties properties) {
        return buildExecutor(properties.getCommon());
    }

    /**
     * 构建线程池的通用方法
     */
    private ThreadPoolExecutor buildExecutor(ThreadPoolConfigProperties.ExecutorProperties props) {
        // 1. 实例化拒绝策略
        RejectedExecutionHandler handler;
        switch (props.getRejectPolicy()) {
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }

        // 2. 自定义线程工厂 (为了设置线程名称前缀，方便排查问题)
        ThreadFactory threadFactory = new NamedThreadFactory(props.getNamePrefix());

        // 3. 创建线程池
        return new ThreadPoolExecutor(
                props.getCorePoolSize(),
                props.getMaxPoolSize(),
                props.getKeepAliveTime(),
                TimeUnit.MILLISECONDS, // 注意：YAML里通常是毫秒，或者跟 keepAliveTime 对齐单位
                new LinkedBlockingQueue<>(props.getBlockQueueSize()),
                threadFactory,
                handler
        );
    }

    /**
     * 简单的命名线程工厂
     */
    static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
