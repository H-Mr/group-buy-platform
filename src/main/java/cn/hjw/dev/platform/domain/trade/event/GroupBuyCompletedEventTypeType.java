package cn.hjw.dev.platform.domain.trade.event;

import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

;


/**
 * 拼团完成事件类型
 * 发布平台内完成事件
 */
@Component
public class GroupBuyCompletedEventTypeType extends BaseEventType<GroupBuyCompletedEventTypeType.GroupBuyCompleted> {

    @Resource(name = "sys-event-bus")
    private EventBus eventBus; // 注入 Guava EventBus

    /**
     * 发布拼团完成事件
     * @param teamId 拼团ID
     */
    public void publishGroupBuyCompleted(String teamId) {
        GroupBuyCompleted payload = GroupBuyCompleted.builder()
                .teamId(teamId)
                .build();
        Message<GroupBuyCompleted> eventMessage = buildEventPayload(payload);
        eventBus.post(eventMessage);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupBuyCompleted {
        String teamId; // 拼团成功的团ID
    }
}
