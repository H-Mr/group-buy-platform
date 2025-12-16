package cn.hjw.dev.platform.domain.order.service.impl;

import cn.hjw.dev.platform.domain.inventory.event.InventoryChangedEventType;
import cn.hjw.dev.platform.domain.inventory.model.valobj.InventoryChangedTypeVO;
import cn.hjw.dev.platform.domain.order.adapter.port.IOrderPort;
import cn.hjw.dev.platform.domain.order.adapter.repository.IOrderRepository;
import cn.hjw.dev.platform.domain.order.model.aggregate.CreateOrderAggregate;
import cn.hjw.dev.platform.domain.order.model.entity.OrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.PayOrderEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.MarketTypeVO;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl extends AbstractOrderService {

    @Resource
    private InventoryChangedEventType inventoryChangedEventType;


    public OrderServiceImpl(IOrderRepository repository, IOrderPort port) {
        super(repository, port);
    }

    /**
     * 保存订单基本信息
     * @param orderAggregate
     */
    @Override
    protected void doSaveOrder(CreateOrderAggregate orderAggregate) {
        repository.doSaveOrder(orderAggregate);
    }

    /**
     * 创建支付订单
     * @param userId
     * @param productId
     * @param productName
     * @param orderId
     * @param deductionAmount
     * @param payAmount
     * @return
     * @throws AlipayApiException
     */
    @Override
    protected PayOrderEntity doPrepayOrder(String userId,
                                           String productId,
                                           String productName,
                                           String orderId,
                                           BigDecimal deductionAmount,
                                           BigDecimal payAmount) {

        // 调用支付宝接口创建支付订单
        String form = port.createAlipayPagePayOrder(orderId, payAmount, productName);

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setOrderId(orderId); // 设置订单ID
        payOrderEntity.setPayUrl(form); // 支付宝返回的form表单
        payOrderEntity.setOrderStatus(OrderStatusVO.PAY_WAIT); // 设置订单状态为待支付
        payOrderEntity.setPayAmount(payAmount);
        payOrderEntity.setMarketDeductionAmount(deductionAmount);

        // 更新订单支付信息
        repository.updateOrderPayInfo(payOrderEntity);

        return payOrderEntity;
    }

    /**
     * 修改订单支付成功状态
     * 在支付宝支付成功的回调中调用
     * @param orderId 修改状态的订单ID
     * @param payTime 支付时间
     */
    @Override
    public void changeOrderPaySuccess(String orderId, LocalDateTime payTime) {
        // 事务方法保证更新pay_order表时幂等性和正确性
        OrderEntity payOrderEntity = repository.changeOrder2Success(orderId, payTime);
        if (payOrderEntity.getMarketType().equals(MarketTypeVO.GROUP_BUY_MARKET)) {
                this.asyncDoSettlement(payOrderEntity);
        } else {
            inventoryChangedEventType.publishInventoryChangedEvent(payOrderEntity.getProductId(), InventoryChangedTypeVO.DECREASE,1);
        }
    }

    /**
     * 异步执行耗时操作（复用全局asyncTaskExecutor线程池）
     * 注：@Async方法不能是private（Spring AOP 无法代理）
     */
    @Async("asyncTaskExecutor") // 指定全局耗时操作线程池
    public void asyncDoSettlement(OrderEntity order) {
        try {
            // 耗时操作1：执行结算
            port.settlementMarketPayOrder(order.getUserId(), order.getOrderId(), order.getPayTime());
        } catch (Exception e) {
            // 异常处理：日志+重试+告警
            log.error("订单{}耗时操作执行失败", order.getOrderId(), e);
            // retryAsyncTask(orderId, paySuccessTime);
        }
    }

    /**
     * 超过1分钟未支付订单
     * 查询未支付通知的订单列表
     * @return
     */
    @Override
    public List<String> queryNoPayNotifyOrder() {
        return repository.queryNoPayNotifyOrder();
    }

    /**
     * 查询超时订单列表
     * @return
     */
    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return repository.queryTimeoutCloseOrderList();
    }

    /**
     * 修改订单关闭状态
     * @param orderId
     * @return
     */
    @Override
    public boolean changeOrderClose(String orderId) {
        return repository.changeOrder2Close(orderId);
    }
}
