package cn.hjw.dev.platform.domain.order.adapter.event;

import cn.hjw.dev.platform.types.event.BaseEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description
 * @create 2024-10-04 09:31
 */
@Component
public class PaySuccessMessageEvent extends BaseEventType<PaySuccessMessageEvent.PaySuccessMessage> {

    /**
     * 构建PaySuccessMessageEvent事件消息
     * @param data
     * @return
     */
    @Override
    public BaseEventType.Message<PaySuccessMessage> buildEventPayload(PaySuccessMessage data) {
        return BaseEventType.Message.<PaySuccessMessage>builder()
                .id(RandomStringUtils.randomNumeric(11))
                .timestamp(LocalDateTime.now())
                .payload(data)
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaySuccessMessage{
        private String userId;
        private String tradeNo;
    }

}
