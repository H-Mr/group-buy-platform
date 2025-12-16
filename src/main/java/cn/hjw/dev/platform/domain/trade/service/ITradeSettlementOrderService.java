package cn.hjw.dev.platform.domain.trade.service;

import cn.hjw.dev.platform.domain.trade.model.entity.TradePaySettlementEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradePaySuccessEntity;

/**
 * 结算服务，启动结算DAG
 */
public interface ITradeSettlementOrderService {

    /**
     * 营销结算
     * @param tradePaySuccessEntity 交易支付订单实体对象
     * @return 交易结算订单实体
     */
    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception;



}
