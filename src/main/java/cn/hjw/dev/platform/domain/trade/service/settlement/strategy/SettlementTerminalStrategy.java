package cn.hjw.dev.platform.domain.trade.service.settlement.strategy;

import cn.hjw.dev.platform.domain.trade.model.entity.TradeSettlementRequest;
import cn.hjw.dev.platform.domain.trade.service.settlement.factory.SettlementDAGFactory;
import cn.hjw.dev.dagflow.processor.TerminalStrategy;
import org.springframework.stereotype.Component;

@Component
public class SettlementTerminalStrategy implements TerminalStrategy<TradeSettlementRequest, SettlementDAGFactory.TradeSettlementContext, SettlementDAGFactory.TradePaySettlementResult> {

    @Override
    public SettlementDAGFactory.TradePaySettlementResult terminate(TradeSettlementRequest req, SettlementDAGFactory.TradeSettlementContext ctx) {
        return ctx.getFinalResult();
    }
}
