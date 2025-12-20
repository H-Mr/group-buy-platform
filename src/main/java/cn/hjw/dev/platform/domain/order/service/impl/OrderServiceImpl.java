package cn.hjw.dev.platform.domain.order.service.impl;

import cn.hjw.dev.platform.domain.order.adapter.port.IOrderPort;
import cn.hjw.dev.platform.domain.order.adapter.repository.IOrderRepository;
import cn.hjw.dev.platform.domain.order.async.OrderAsyncTaskExecutor;
import cn.hjw.dev.platform.domain.order.model.aggregate.CreateOrderAggregate;
import cn.hjw.dev.platform.domain.order.model.entity.OrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.PayOrderEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import cn.hjw.dev.platform.domain.order.model.valobj.SettlementMarketPayOrderVO;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl extends AbstractOrderService {


    @Resource
    private OrderAsyncTaskExecutor orderAsyncTaskExecutor;


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
     * @param productName
     * @param orderId
     * @param deductionAmount
     * @param payAmount
     * @return
     * @throws AlipayApiException
     */
    @Override
    protected PayOrderEntity doPrepayOrder(String orderId,
                                           BigDecimal deductionAmount,
                                           BigDecimal payAmount,
                                           String productName,
                                           String source,
                                           String channel) {

        // 调用支付宝接口创建支付订单
        String form = port.createAlipayPagePayOrder(orderId, payAmount, productName,source, channel);

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setOrderId(orderId); // 设置订单ID
        payOrderEntity.setPayUrl(form); // 支付宝返回的form表单
        // 更新订单支付信息
        repository.updateOrder2WaitPay(payOrderEntity);

        return payOrderEntity;
    }

    /**
     * 修改订单支付成功状态
     * 在支付宝支付成功的回调中调用
     * @param orderId 修改状态的订单ID
     * @param payTime 支付时间
     */
    @Override
    public void changeOrderPaySuccess(String orderId, LocalDateTime payTime, String source, String channel) {
        OrderEntity order = repository.queryPayOrderById(orderId);
        SettlementMarketPayOrderVO settlementMarketPayOrderVO = SettlementMarketPayOrderVO.builder()
                .tradeTime(payTime)
                .orderId(orderId)
                .userId(order.getUserId())
                .source(source)
                .channel(channel)
                .build();
        port.settlementMarketPayOrder(settlementMarketPayOrderVO); // 默认都是营销订单，异步执行结算操作
        repository.changeOrder2Success(orderId, payTime);

        log.info("订单{}支付成功，支付时间{}", orderId, payTime);
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
