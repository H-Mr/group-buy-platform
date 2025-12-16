package cn.hjw.dev.platform.domain.trade.service.settlement.processors;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeSettlementRequest;
import cn.hjw.dev.platform.domain.trade.service.settlement.factory.SettlementDAGFactory;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class ValidateTeamProcessor implements DAGNodeProcessor<TradeSettlementRequest, SettlementDAGFactory.TradeSettlementContext, SettlementDAGFactory.TradeSettlementNodeResult> {

    @Resource
    private ITradeRepository repository;

    @Override
    public SettlementDAGFactory.TradeSettlementNodeResult process(TradeSettlementRequest req, SettlementDAGFactory.TradeSettlementContext ctx) {
        // 依赖 Level 0 的结果
        MarketPayOrderEntity order = ctx.getPayOrder();
        log.info("DAG-Settlement: 加载拼团信息 teamId:{}", order.getTeamId());
        // 查询参与团的信息，校验拼团有效时间
        GroupBuyTeamEntity team = repository.queryGroupBuyTeamByTeamId(order.getTeamId());

        // 校验交易时间
        if (req.getOutTradeTime().isBefore(team.getValidStartTime()) || req.getOutTradeTime().isAfter(team.getValidEndTime())) {
            log.error("订单交易时间不在拼团有效时间范围内");
            throw new AppException(ResponseCode.E0106);
        }

        return new SettlementDAGFactory.TradeSettlementNodeResult(SettlementDAGFactory.TradeSettlementNodeResult.TYPE_TEAM, team);
    }
}
