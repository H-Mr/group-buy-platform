package cn.hjw.dev.platform.domain.trade.service.lock.processors;

import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;
import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import cn.hjw.dev.platform.domain.trade.model.entity.*;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class LockPersistProcessor implements DAGNodeProcessor<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, LockOrderDAGFactory.TradeLockNodeResult> {

    @Resource
    private ITradeRepository tradeRepository;

    @Override
    public LockOrderDAGFactory.TradeLockNodeResult process(TradeLockRequestEntity req, LockOrderDAGFactory.TradeLockContext ctx) throws NoSuchAlgorithmException {
        log.info("DAG-Lock: 执行落库锁定");

        TrialBalanceEntity trial = ctx.getTrialResult();
        GroupBuyActivityEntity activity = ctx.getActivity();

        // 1. 构建 PayActivityEntity
        PayActivityEntity payActivity = PayActivityEntity.builder()
                .activityId(activity.getActivityId())
                .activityName(activity.getActivityName())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .validTime(activity.getValidTime())
                .targetCount(activity.getTarget())
                .teamId(req.getTeamId())
                .build();

        // 2. 构建 PayDiscountEntity (数据来自试算结果)
        PayDiscountEntity payDiscount = PayDiscountEntity.builder()
                .source(req.getSource())
                .channel(req.getChannel())
                .goodsId(req.getGoodsId())
                .goodsName(trial.getGoodsName())
                .originalPrice(trial.getOriginalPrice())
                .deductionPrice(trial.getDeductionPrice())
                .payPrice(trial.getPayPrice())
                .outTradeNo(req.getOutTradeNo())
                .notifyUrl(req.getNotifyUrl())
                .build();

        // 3. 构建聚合对象
        GroupBuyOrderAggregate aggregate = GroupBuyOrderAggregate.builder()
                .userEntity(UserEntity.builder().userId(req.getUserId()).build())
                .payActivityEntity(payActivity)
                .payDiscountEntity(payDiscount)
                .userTakeOrderCount(ctx.getUserTakeCount())
                .build();

        // 4. 调用仓储落库 (事务操作)
        MarketPayOrderEntity order = tradeRepository.lockMarketPayOrder(aggregate);

        return new LockOrderDAGFactory.TradeLockNodeResult(LockOrderDAGFactory.TradeLockNodeResult.TYPE_PERSIST, order);
    }

}
