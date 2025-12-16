package cn.hjw.dev.platform.domain.order.adapter.repository;

import cn.hjw.dev.platform.domain.order.model.aggregate.CreateOrderAggregate;
import cn.hjw.dev.platform.domain.order.model.entity.OrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.PayOrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ShopCartEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface IOrderRepository {


    void doSaveOrder(CreateOrderAggregate orderAggregate);

    OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity);

    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    OrderEntity changeOrder2Success(String orderId, LocalDateTime payTime);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrder2Close(String orderId);

    OrderEntity queryPayOrderById(String orderId);
}
