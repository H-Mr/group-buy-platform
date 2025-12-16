package cn.hjw.dev.platform.app.config;

import cn.hjw.dev.platform.infrastructure.dcc.event.DccUpdateEventTypeType;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * DCC Redis 适配器
 * 负责将 Redis 消息转换为系统内部事件。
 */

@Slf4j
@Configuration
public class DccRedisAdapter {

    @Resource
    DccUpdateEventTypeType dccUpdateEventType;

    @Bean("dccTopic")
    public RTopic dccRedisTopicListener(RedissonClient redissonClient) {
        RTopic topic = redissonClient.getTopic("group_buy_market_dcc");

        topic.addListener(String.class, (channel, message) -> {
            log.info("DCC 收到 Redis 推送: {}", message);
            try {
                // 协议格式：key,value (注意这里改成逗号或等号，与 Controller 保持一致)
                // 建议 Controller 发送时使用 "," 分隔，因为 Value 可能包含 "="
                String[] split = message.split(",");
                if (split.length != 2) return;

                // 转发为内部事件 -> AsyncEventBus -> Listener
                dccUpdateEventType.publishDccUpdateEvent(split[0], split[1]);

            } catch (Exception e) {
                log.error("DCC 消息处理异常", e);
            }
        });
        return topic;
    }
}
