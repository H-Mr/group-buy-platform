package cn.hjw.dev.platform.domain.auth.event;

import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class WeixinLoginSuccessEventTypeType extends BaseEventType<WeixinLoginSuccessEventTypeType.WeiXinLoginSuccess> {

    @Resource(name = "sys-event-bus")
    private EventBus eventBus; // 注入 Guava EventBus

    /**
     * 发布拼团完成事件
     * @param openid 微信用户唯一标识
     */
    public void publishWeiXinSuccessEvent(String openid) {
        WeiXinLoginSuccess payload = WeiXinLoginSuccess.builder()
                .openid(openid)
                .build();
        Message<WeiXinLoginSuccess> eventMessage = buildEventPayload(payload);
        eventBus.post(eventMessage);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WeiXinLoginSuccess {
        private String openid;

    }
}
