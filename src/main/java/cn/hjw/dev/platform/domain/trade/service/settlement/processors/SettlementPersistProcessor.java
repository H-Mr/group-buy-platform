package cn.hjw.dev.platform.domain.trade.service.settlement.processors;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.hjw.dev.platform.domain.trade.model.entity.*;
import cn.hjw.dev.platform.domain.trade.service.settlement.factory.SettlementDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class SettlementPersistProcessor implements DAGNodeProcessor<TradeSettlementRequest, SettlementDAGFactory.TradeSettlementContext, SettlementDAGFactory.TradeSettlementNodeResult> {
    @Resource private ITradeRepository repository;

    @Override
    public SettlementDAGFactory.TradeSettlementNodeResult process(TradeSettlementRequest req, SettlementDAGFactory.TradeSettlementContext ctx) {
        log.info("DAG-Settlement: 执行结算落库");

        GroupBuyTeamEntity team = ctx.getTeam();

        // 构建聚合对象
        GroupBuyTeamSettlementAggregate aggregate = GroupBuyTeamSettlementAggregate.builder()
                .userEntity(UserEntity.builder().userId(req.getUserId()).build())
                .groupBuyTeamEntity(team)
                .tradePaySuccessEntity(TradePaySuccessEntity.builder()
                        .source(req.getSource())
                        .channel(req.getChannel())
                        .userId(req.getUserId())
                        .outTradeNo(req.getOutTradeNo())
                        .outTradeTime(req.getOutTradeTime())
                        .build())
                .build();

        // 执行数据库结算 (Repository 内部应处理事务和状态流转)
        boolean isCompleted = repository.settlementMarketPayOrder(aggregate);

        // 构建返回实体
        TradePaySettlementEntity res = TradePaySettlementEntity.builder()
                .source(req.getSource())
                .channel(req.getChannel())
                .userId(req.getUserId())
                .teamId(team.getTeamId())
                .activityId(team.getActivityId())
                .outTradeNo(req.getOutTradeNo())
                .build();
        // 保存上下文
        SettlementDAGFactory.TradePaySettlementResult result = SettlementDAGFactory.TradePaySettlementResult.builder()
                .isCompleted(isCompleted)
                .settlementEntity(res)
                .build();

        return new SettlementDAGFactory.TradeSettlementNodeResult(SettlementDAGFactory.TradeSettlementNodeResult.TYPE_PERSIST, result);
    }


}
