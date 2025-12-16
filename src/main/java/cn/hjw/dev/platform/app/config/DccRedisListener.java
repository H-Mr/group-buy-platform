package cn.hjw.dev.platform.app.config;

import cn.hjw.dev.platform.infrastructure.dcc.DccValueManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * * DCC Redis 监听器配置
 */
@Slf4j
@Configuration
public class DccRedisListener {

    @Resource
    private DccValueManager dccValueManager;

    /**
     * DCC Redis 主题监听器,监听redis的 DCC 配置变更消息
     * @param redissonClient
     * @return
     */
    @Bean("dccTopic")
    public RTopic dccRedisTopicListener(RedissonClient redissonClient) {
        RTopic topic = redissonClient.getTopic("group_buy_market_dcc");

        topic.addListener(String.class, (channel, message) -> {
            log.info("DCC 收到 Redis 推送: {}", message);
            try {
                // 协议格式：key,value
                String[] split = message.split(",");
                if (split.length != 2) return; // 格式错误直接丢弃
                dccValueManager.refresh(split[0], split[1]);
                log.info("DCC 配置更新完成: {} -> {}", split[0], split[1]);

            } catch (Exception e) {
                log.error("DCC 更新异常", e);
            }
        });
        return topic;
    }
}
