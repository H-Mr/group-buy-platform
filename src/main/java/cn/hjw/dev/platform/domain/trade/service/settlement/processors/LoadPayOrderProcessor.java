package cn.hjw.dev.platform.domain.trade.service.settlement.processors;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeSettlementRequest;
import cn.hjw.dev.platform.domain.trade.service.settlement.factory.SettlementDAGFactory;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 加载支付订单处理器
 */
@Slf4j
@Component
public class LoadPayOrderProcessor implements DAGNodeProcessor<TradeSettlementRequest, SettlementDAGFactory.TradeSettlementContext, SettlementDAGFactory.TradeSettlementNodeResult> {

    @Resource
    private ITradeRepository repository;

    @Override
    public SettlementDAGFactory.TradeSettlementNodeResult process(TradeSettlementRequest req, SettlementDAGFactory.TradeSettlementContext ctx) {
        log.info("DAG-Settlement: 加载结算的本地支付订单 outTradeNo:{}", req.getOutTradeNo());
        // 从拼团流水单中加载支付订单
        MarketPayOrderEntity payOrder = repository.queryMarketPayOrderEntityByOutTradeNo(req.getUserId(), req.getOutTradeNo());
        if (payOrder == null) {
            log.error("不存在的外部交易单号或用户已退单: {}", req.getOutTradeNo());
            throw new AppException(ResponseCode.E0104);
        }
        // 计算支付金额
        payOrder.setPayPrice(payOrder.getOriginalPrice().subtract(payOrder.getDeductionPrice()));
        return new SettlementDAGFactory.TradeSettlementNodeResult(SettlementDAGFactory.TradeSettlementNodeResult.TYPE_ORDER, payOrder);
    }
}
