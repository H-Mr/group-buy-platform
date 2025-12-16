package cn.hjw.dev.platform.domain.trade.service;

import cn.hjw.dev.platform.domain.trade.model.entity.*;

/**
 * 锁定营销订单服务接口，启动锁单DAG
 */
public interface ITradeLockOrderService {

    /**
     * 查询，未被支付消费完成的营销优惠订单
     *
     * @param userId     用户ID
     * @param outTradeNo 外部唯一单号
     * @return 拼团，预购订单营销实体对象
     */
    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    /**
     * 锁定，营销预支付订单；商品下单前，预购锁定。
     * @param tradeLockRequest 锁单请求实体
     * @return 拼团，预购订单营销实体对象
     */
    MarketPayOrderEntity lockMarketPayOrder(TradeLockRequestEntity tradeLockRequest) throws Exception;

}
