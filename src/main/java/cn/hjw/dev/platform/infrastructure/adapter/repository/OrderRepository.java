package cn.hjw.dev.platform.infrastructure.adapter.repository;

import cn.hjw.dev.platform.domain.order.adapter.repository.IOrderRepository;
import cn.hjw.dev.platform.domain.order.model.aggregate.CreateOrderAggregate;
import cn.hjw.dev.platform.domain.order.model.entity.OrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.PayOrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ShopCartEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.MarketTypeVO;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import cn.hjw.dev.platform.infrastructure.dao.IOrderDao;
import cn.hjw.dev.platform.infrastructure.dao.po.PayOrder;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private IOrderDao orderDao;

    /**
     * 保存新订单
     * @param orderAggregate
     */
    @Override
    public void doSaveOrder(CreateOrderAggregate orderAggregate) {

        OrderEntity orderEntity = orderAggregate.getOrderEntity();

        PayOrder order = new PayOrder();
        order.setUserId(orderEntity.getUserId()); // 用户ID
        order.setProductId(orderEntity.getProductId()); // 商品ID
        order.setProductName(orderEntity.getProductName()); // 商品名称
        order.setOrderId(orderEntity.getOrderId()); // 订单ID
        order.setOrderTime(orderEntity.getOrderTime()); // 订单创建时间
        order.setTotalAmount(orderEntity.getTotalAmount()); // 订单总金额
        order.setPayAmount(orderEntity.getPayAmount()); // 支付金额
        order.setMarketDeductionAmount(orderEntity.getMarketDeductionAmount()); // 营销优惠金额
        order.setMarketType(orderEntity.getMarketType().getCode()); // 营销类型
        order.setStatus(orderEntity.getOrderStatusVO().getCode()); // 订单状态

        try {
            orderDao.insert(order);
        } catch (DuplicateKeyException e) {
            // 订单已存在，忽略重复插入
        }
    }

    /**
     * 查询需要下单的商品是否有未支付的订单
     * @param shopCartEntity
     * @return
     */
    @Override
    public OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity) {
        // 1. 封装参数
        PayOrder orderReq = new PayOrder();
        orderReq.setUserId(shopCartEntity.getUserId()); // 查询未支付订单时，需指定用户ID
        orderReq.setProductId(shopCartEntity.getProductId()); // 查询未支付订单时，需指定商品ID

        // 2. 查询到订单
        PayOrder order = orderDao.queryUnPayOrder(orderReq);
        if (null == order) return null; // 不存在未支付订单

        // 3. 返回结果
        return OrderEntity.builder()
                .productId(order.getProductId())
                .productName(order.getProductName())
                .orderId(order.getOrderId())
                .orderStatusVO(OrderStatusVO.valueOf(order.getStatus()))
                .marketType(MarketTypeVO.valueOf(order.getMarketType()))
                .orderTime(order.getOrderTime())
                .totalAmount(order.getTotalAmount())
                .marketDeductionAmount(order.getMarketDeductionAmount())
                .payAmount(order.getPayAmount())
                .payUrl(order.getPayUrl())
                .build();
    }

    /**
     * 查询未收到支付通知的订单列表
     * @return
     */
    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderDao.queryNoPayNotifyOrder();
    }

    /**
     * 查询超时关闭订单列表
     * @return
     */
    @Override
    public List<String> queryTimeoutCloseOrderList() {
        List<String> orderList = orderDao.queryTimeoutCloseOrderList();
        return ObjectUtils.isEmpty(orderList) ? Collections.emptyList() : orderList;
    }

    /**
     * 根据订单ID查询支付订单
     * @param orderId
     * @return
     */
    @Override
    public OrderEntity queryPayOrderById(String orderId) {
        PayOrder payedOrder = orderDao.queryPayOrderById(orderId);
        // 3. 返回结果
        return OrderEntity.builder()
                .productId(payedOrder.getProductId())
                .productName(payedOrder.getProductName())
                .orderId(payedOrder.getOrderId())
                .orderStatusVO(OrderStatusVO.valueOf(payedOrder.getStatus()))
                .orderTime(payedOrder.getOrderTime())
                .marketType(MarketTypeVO.valueOf(payedOrder.getMarketType()))
                .userId(payedOrder.getUserId())
                .marketDeductionAmount(payedOrder.getMarketDeductionAmount())
                .totalAmount(payedOrder.getTotalAmount())
                .payTime(payedOrder.getPayTime())
                .payUrl(payedOrder.getPayUrl())
                .build();
    }




    /**
     * 补充待支付信息
     * @param payOrderEntity
     */
    @Override
    public void updateOrder2WaitPay(PayOrderEntity payOrderEntity) {
        PayOrder updateOrder = PayOrder.builder()
                        .payUrl(payOrderEntity.getPayUrl())
                        .status(OrderStatusVO.PAY_WAIT.getCode())
                        .build();
        PayOrder condition = new PayOrder();
        condition.setOrderId(payOrderEntity.getOrderId());
        condition.setStatus(OrderStatusVO.CREATE.getCode());



        orderDao.updatePayOrderByCondition(updateOrder,condition);
    }

    /**
     * 修改订单支付成功状态
     * 在支付宝支付成功的回调中调用
     * @param orderId 修改状态的订单ID
     * @param payTime 支付时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderEntity changeOrder2Success(String orderId, LocalDateTime payTime) {

        PayOrder updatedOrder = new PayOrder();
        updatedOrder.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        updatedOrder.setPayTime(payTime);

        PayOrder condOrder = new PayOrder();
        condOrder.setOrderId(orderId); // 订单ID
        condOrder.setStatus(OrderStatusVO.PAY_WAIT.getCode()); // 只更新待支付状态的订单

        PayOrder payedOrder = orderDao.queryPayOrderIdForUpdate(condOrder);
        if (Objects.isNull(payedOrder)) {
            // 订单已支付或已关闭，直接返回空
            return null;
        }

        orderDao.updatePayOrderByCondition(updatedOrder, condOrder);

        return  OrderEntity.builder()
                .productId(payedOrder.getProductId())
                .productName(payedOrder.getProductName())
                .orderId(payedOrder.getOrderId())
                .orderStatusVO(OrderStatusVO.valueOf(payedOrder.getStatus()))
                .orderTime(payedOrder.getOrderTime())
                .marketType(MarketTypeVO.valueOf(payedOrder.getMarketType()))
                .userId(payedOrder.getUserId())
                .marketDeductionAmount(payedOrder.getMarketDeductionAmount())
                .totalAmount(payedOrder.getTotalAmount())
                .payTime(payedOrder.getPayTime())
                .payUrl(payedOrder.getPayUrl())
                .build();
    }


    /**
     * 变更订单关闭状态
     * @param orderId
     * @return
     */
    @Override
    @Transactional
    public boolean changeOrder2Close(String orderId) {

        PayOrder updatedOrder = new PayOrder();
        updatedOrder.setStatus(OrderStatusVO.CLOSE.getCode());

        PayOrder condOrder = new PayOrder();
        condOrder.setOrderId(orderId); // 订单ID
        condOrder.setStatus(OrderStatusVO.PAY_WAIT.getCode()); // 只更新待支付状态的订单


        PayOrder waitClosedOrder = orderDao.queryPayOrderIdForUpdate(condOrder);
        if (Objects.isNull(waitClosedOrder)) {
            // 已关闭，直接返回
            return true;
        }
        return orderDao.updatePayOrderByCondition(updatedOrder, condOrder) == 1;
    }

}
