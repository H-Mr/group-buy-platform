package cn.hjw.dev.platform.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池配置属性类
 * 对应 YAML 中的 thread.pool.*
 */
@Data
@ConfigurationProperties(prefix = "thread.pool")
public class ThreadPoolConfigProperties {

    /** DAG 核心业务线程池配置 */
    private ExecutorProperties dag;

    /** EventBus 通知业务线程池配置 */
    private ExecutorProperties notify;

    private ExecutorProperties common;

    /**
     * 通用线程池参数定义
     */
    @Data
    public static class ExecutorProperties {
        /** 核心线程数 */
        private Integer corePoolSize = 20;
        /** 最大线程数 */
        private Integer maxPoolSize = 200;
        /** 最大等待时间 (秒) */
        private Long keepAliveTime = 10L;
        /** 阻塞队列容量 */
        private Integer blockQueueSize = 5000;
        /** 拒绝策略 (AbortPolicy, DiscardPolicy, DiscardOldestPolicy, CallerRunsPolicy) */
        private String rejectPolicy = "AbortPolicy";
        /** 线程名称前缀 (便于监控和日志排查) */
        private String namePrefix = "default-executor-";
    }
}
