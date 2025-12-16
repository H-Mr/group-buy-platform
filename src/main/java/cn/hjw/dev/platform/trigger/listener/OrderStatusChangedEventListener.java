package cn.hjw.dev.platform.trigger.listener;

import cn.hjw.dev.platform.domain.order.event.OrderStatusChangedEventType;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import cn.hjw.dev.platform.domain.order.service.IOrderService;
import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
@Slf4j
public class OrderStatusChangedEventListener {

    @Resource(name = "sys-event-bus")
    private EventBus eventBus;

    @Resource
    private IOrderService orderService;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onOrderStatusChangedEvent(BaseEventType.Message<OrderStatusChangedEventType.ChangedOrder> event) {
        // 第一步：强制校验载荷类型，非目标类型直接返回
        Object payload = event.getPayload();
        if (!(payload instanceof OrderStatusChangedEventType.ChangedOrder)) {
            return;
        }
        // 第二步：安全强转
        OrderStatusChangedEventType.ChangedOrder order = (OrderStatusChangedEventType.ChangedOrder) payload;

        log.info("接收到订单操作变更事件，订单ID：{}，订单状态：{}", order.getOrderId(), order.getOrderStatus());
        if(OrderStatusVO.CLOSE.equals(order.getOrderStatus())) {
            // 超时关单
            orderService.changeOrderClose(order.getOrderId());
        } else {
            // 补充支付成功
            orderService.changeOrderPaySuccess(order.getOrderId(), order.getPaySuccessTime());
        }
    }

}
