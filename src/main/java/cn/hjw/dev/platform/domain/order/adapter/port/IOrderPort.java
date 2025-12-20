package cn.hjw.dev.platform.domain.order.adapter.port;

import cn.hjw.dev.platform.domain.order.model.entity.ProductEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.GroupMarketProductPriceVO;
import cn.hjw.dev.platform.domain.order.model.valobj.LockMarketPayOrderVO;
import cn.hjw.dev.platform.domain.order.model.valobj.SettlementMarketPayOrderVO;

import java.math.BigDecimal;
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
     */
    void settlementMarketPayOrder(SettlementMarketPayOrderVO settlementVo);

    /**
     * 创建支付宝页面支付订单
     * @param orderId 订单ID
     * @param payAmount 支付金额
     * @param productName 商品名称
     * @return 支付宝页面支付表单字符串
     */
    String createAlipayPagePayOrder(String orderId, BigDecimal payAmount, String productName,String source, String channel);
}
