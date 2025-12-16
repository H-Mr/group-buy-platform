package cn.hjw.dev.platform.infrastructure.dcc.event;

import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DCC更新类型事件
 * 类型固定，每种事件可以携带不同的Payload数据
 */

@Component
public class DccUpdateEventTypeType extends BaseEventType<DccUpdateEventTypeType.DccConfig> {

    @Resource(name = "dcc-event-bus")
    private EventBus eventBus; // 注入异步 EventBus
    /**
     * 发布DCC配置更新事件
     * @param key 配置键
     * @param value 配置值
     */
    public void publishDccUpdateEvent(String key, String value) {
        DccConfig payload = new DccConfig(key, value);
        eventBus.post(this.buildEventPayload(payload));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DccConfig {
        private String key;
        private String value;
    }
}
