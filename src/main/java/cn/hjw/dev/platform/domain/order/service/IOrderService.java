package cn.hjw.dev.platform.domain.order.service;

import cn.hjw.dev.platform.domain.order.model.entity.PayOrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ShopCartEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {

    PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception;

    void changeOrderPaySuccess(String orderId, LocalDateTime payTime);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

}
