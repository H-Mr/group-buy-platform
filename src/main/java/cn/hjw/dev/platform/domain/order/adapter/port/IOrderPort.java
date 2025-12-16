package cn.hjw.dev.platform.domain.order.adapter.port;

import cn.hjw.dev.platform.domain.order.model.entity.ProductEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.GroupMarketProductPriceVO;
import cn.hjw.dev.platform.domain.order.model.valobj.LockMarketPayOrderVO;

import java.time.LocalDateTime;

public interface IOrderPort {

    /**
     * 根据商品ID查询商品信息
     * @param productId
     * @return
     */
    ProductEntity queryProductByProductId(String productId);

    /**
     * 锁定营销订单
     * @param lockMarketPayOrderVO 锁单信息对象
     * @return
     */
    GroupMarketProductPriceVO lockMarketPayOrder(LockMarketPayOrderVO lockMarketPayOrderVO);

    /**
     * 结算营销订单
     * @param userId 结算的用户ID
     * @param orderId 结算的订单ID
     * @param tradeTime 交易时间
     */
    void settlementMarketPayOrder(String userId, String orderId, LocalDateTime tradeTime);

    /**
     * 创建支付宝页面支付订单
     * @param orderId 订单ID
     * @param payAmount 支付金额
     * @param productName 商品名称
     * @return 支付宝页面支付表单字符串
     */
    String createAlipayPagePayOrder(String orderId, java.math.BigDecimal payAmount, String productName);
}
