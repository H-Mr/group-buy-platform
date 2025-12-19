package cn.hjw.dev.platform.trigger.job;

import cn.hjw.dev.platform.domain.order.event.OrderStatusChangedEventType;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import cn.hjw.dev.platform.domain.order.service.IOrderService;
import cn.hjw.dev.platform.infrastructure.gateway.AlipayRequestGateway;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 定时扫描指定时间内未支付的订单，发布事件
 */
@Slf4j
@Service
public class TimeoutScanOrderJob {

    @Resource
    private OrderStatusChangedEventType orderStatusChangedEventType;

    @Resource
    private IOrderService orderService; // 拉取15分钟以后的未支付订单

    @Resource
    private AlipayRequestGateway alipayRequestGateway; // 查询订单支付状态

    @Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次，在每 5 分钟的 “第 0 秒” 执行一次任务
    public void exec() {
        // 定时查询超过15分钟未支付的订单，发布事件
        log.info("任务；超时15分钟订单关闭");
        // 1. 获取任务列表（这一步如果挂了，确实没法跑，可以在最外层catch）
        List<String> orderIds;
        try {
            orderIds = orderService.queryTimeoutCloseOrderList();
        } catch (Exception e) {
        log.error("定时任务拉取订单失败", e);
        return;
        }
        if (orderIds.isEmpty()) return;
        for (String orderId : orderIds) {
            //
           try {
               AlipayRequestGateway.AlipayOrderQueryResult queryResult = alipayRequestGateway.queryOrderPayStatus(orderId);
               if (ObjectUtils.isNotEmpty(queryResult.isPaySuccess()) && queryResult.isPaySuccess()) {
                   LocalDateTime payTime = LocalDateTime.parse(
                           queryResult.getPayTime(),
                           DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                   );
                   orderStatusChangedEventType.publishOrderStatusChangedEvent(orderId, OrderStatusVO.PAY_SUCCESS,payTime);
               } else {
                   alipayRequestGateway.closeAlipayOrder(orderId); // 关闭支付宝订单
                   orderStatusChangedEventType.publishOrderStatusChangedEvent(orderId, OrderStatusVO.CLOSE,null);
               }
           } catch (Exception e) {
                 log.error("处理超时未支付订单失败，orderId：{}", orderId, e);
           }
        }
    }

}
