package cn.hjw.dev.platform.domain.trade.service.settlement.processors;

import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeSettlementRequest;
import cn.hjw.dev.platform.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.hjw.dev.platform.domain.trade.service.settlement.factory.SettlementDAGFactory;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 资金安全与幂等检查处理器
 */
@Slf4j
@Component
public class IntegrityCheckProcessor implements DAGNodeProcessor<TradeSettlementRequest, SettlementDAGFactory.TradeSettlementContext, SettlementDAGFactory.TradeSettlementNodeResult> {

    @Override
    public SettlementDAGFactory.TradeSettlementNodeResult process(TradeSettlementRequest req, SettlementDAGFactory.TradeSettlementContext ctx) {
        log.info("DAG-Settlement: 执行资金安全与幂等检查");
        MarketPayOrderEntity order = ctx.getPayOrder();

        // 1. 幂等性检查
        // 如果订单状态已经是 "消费完成"，说明是重复回调
        if (order.getTradeOrderStatusEnumVO() != null && TradeOrderStatusEnumVO.COMPLETE.equals(order.getTradeOrderStatusEnumVO())) {
            log.warn("订单已完成，触发幂等保护: {}", req.getOutTradeNo());
            throw new AppException(ResponseCode.SUCCESS.getCode(), "订单已处理");
        }

        // 2. 资金安全检查
        // 防止负数金额注入攻击或数据异常
        if (order.getPayPrice().compareTo(BigDecimal.ZERO) < 0) {
            log.error("资金异常：支付金额小于0, orderId:{}", order.getOrderId());
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "支付金额异常");
        }

        return new SettlementDAGFactory.TradeSettlementNodeResult(SettlementDAGFactory.TradeSettlementNodeResult.TYPE_INTEGRITY, true);
    }
}
