package cn.hjw.dev.platform.domain.order.event;

import cn.hjw.dev.platform.api.dto.PaySuccessNotifyDTO;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 超时扫描订单事件类型
 * 1. 扫描所有wait状态的订单，先查询支付宝沙箱支付状态；
 * 2.1 支付成功，因为异常导致回调更新失败的，更新订单状态为已支付；
 * 2.2 超时未支付的，关闭订单。
 */
@Component
public class OrderStatusChangedEventType extends BaseEventType<OrderStatusChangedEventType.ChangedOrder> {

    @Resource(name = "sys-event-bus")
    private EventBus eventBus; // 注入 Guava EventBus

    /**
     * 发布拼团完成事件
     * @param orderId 订单ID
     * @param orderStatus 订单状态
     */
    public void publishOrderStatusChangedEvent(PaySuccessNotifyDTO paySuccessNotifyDTO) {
         ChangedOrder payload = ChangedOrder.builder()
                .orderId(paySuccessNotifyDTO.getTradeNo())
                .orderStatus(paySuccessNotifyDTO.getOrderStatusVO())
                .paySuccessTime(paySuccessNotifyDTO.getPayTime())
                .channel(paySuccessNotifyDTO.getChannel())
                .source(paySuccessNotifyDTO.getSource())
                .build();
        Message<ChangedOrder> eventMessage = buildEventPayload(payload);
        eventBus.post(eventMessage);
    }




    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChangedOrder {

        OrderStatusVO orderStatus; // 本次处理对订单的状态变更
        String orderId; // 待处理状态的订单ID
        LocalDateTime paySuccessTime; // 支付成功时间
        String source; // 订单来源
        String channel; // 订单渠道
    }


}
