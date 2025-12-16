package cn.hjw.dev.platform.domain.trade.service.settlement.strategy;

import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeSettlementRequest;
import cn.hjw.dev.platform.domain.trade.service.settlement.factory.SettlementDAGFactory;
import cn.hjw.dev.dagflow.processor.UpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class SettlementUpdateStrategy implements UpdateStrategy<TradeSettlementRequest, SettlementDAGFactory.TradeSettlementContext, SettlementDAGFactory.TradeSettlementNodeResult> {

    @Override
    public void updateContext(TradeSettlementRequest req, SettlementDAGFactory.TradeSettlementContext ctx, SettlementDAGFactory.TradeSettlementNodeResult res) {
        if (res == null) return;
        switch (res.getType()) {
            case SettlementDAGFactory.TradeSettlementNodeResult.TYPE_ORDER:
                ctx.setPayOrder((MarketPayOrderEntity) res.getData());
                break;
            case SettlementDAGFactory.TradeSettlementNodeResult.TYPE_TEAM:
                ctx.setTeam((GroupBuyTeamEntity) res.getData());
                break;
            case SettlementDAGFactory.TradeSettlementNodeResult.TYPE_PERSIST:
                ctx.setFinalResult((SettlementDAGFactory.TradePaySettlementResult) res.getData());
                break;
        }
    }
}
