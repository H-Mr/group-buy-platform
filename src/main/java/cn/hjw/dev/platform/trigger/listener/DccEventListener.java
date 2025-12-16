package cn.hjw.dev.platform.trigger.listener;

import cn.hjw.dev.platform.infrastructure.dcc.DccValueManager;
import cn.hjw.dev.platform.infrastructure.dcc.event.DccUpdateEventTypeType;
import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class DccEventListener {

    @Resource(name = "dcc-event-bus")
    private EventBus eventBus;

    @Resource
    private DccValueManager dccValueManager;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onUpdate(BaseEventType.Message<DccUpdateEventTypeType.DccConfig> event) {
        Object payload = event.getPayload();
        if (!(payload instanceof DccUpdateEventTypeType.DccConfig)) {
            return;
        }
        DccUpdateEventTypeType.DccConfig config = (DccUpdateEventTypeType.DccConfig) payload;
        log.info("DCC EventBus 触发更新: {} -> {}", config.getKey(), config.getValue());
        // 执行更新
        dccValueManager.refresh(config.getKey(), config.getValue());
    }
}
