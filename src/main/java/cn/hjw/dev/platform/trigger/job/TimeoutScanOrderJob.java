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

    /*
    todo: 待重构

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
               // 1. 先查询状态 (这一步是基础检查)
               AlipayRequestGateway.AlipayOrderQueryResult queryResult = alipayRequestGateway.queryOrderPayStatus(orderId);
               if (queryResult.isPaySuccess()) {
                   LocalDateTime payTime = LocalDateTime.parse(
                           queryResult.getPayTime(),
                           DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                   );
                   orderStatusChangedEventType.publishOrderStatusChangedEvent(orderId, OrderStatusVO.PAY_SUCCESS,payTime);
                   continue;
               }
               // 3. 查出来不是成功 (可能是 WAIT_BUYER_PAY 或 NOT_EXIST)，尝试【强制关单】
               // 这一步是解决竞态的关键：以关单结果为准
               boolean closeSuccess = alipayRequestGateway.closeAlipayOrder(orderId);
               if (closeSuccess) {
                   // A. 关单成功 -> 说明用户确实没付，或者根本没扫码
                   log.info("超时未支付，关单成功，orderId: {}", orderId);
                   orderStatusChangedEventType.publishOrderStatusChangedEvent(orderId, OrderStatusVO.CLOSE, null);
                   continue;
               }
               // B. 关单失败 -> 说明用户刚刚支付了 (ACQ.TRADE_STATUS_ERROR)
               // 此时应该通过查询接口再次确认支付时间，或者直接补偿为支付成功
               log.info("超时关单失败（订单可能已支付），触发补偿查询，orderId: {}", orderId);
               // 二次确认：既然关不掉，说明肯定付了，再查一次拿支付时间
               AlipayRequestGateway.AlipayOrderQueryResult reQueryResult = alipayRequestGateway.queryOrderPayStatus(orderId);
               if (reQueryResult.isPaySuccess()) {
                   LocalDateTime payTime = LocalDateTime.parse(reQueryResult.getPayTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                   orderStatusChangedEventType.publishOrderStatusChangedEvent(orderId, OrderStatusVO.PAY_SUCCESS, payTime);
               } else {
                   // 极少见的情况：关不掉，查也是未支付？可能是支付宝系统抖动，打印日志人工介入
                   log.error("异常：订单无法关闭且未支付成功，需人工排查。orderId: {}", orderId);
               }
           } catch (Exception e) {
                 log.error("处理超时未支付订单失败，orderId：{}", orderId, e);
           }
        }
    }


     */

}
