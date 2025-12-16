package cn.hjw.dev.platform.domain.trade.service.lock.processors;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@Slf4j
public class LoadLockActivityProcessor implements DAGNodeProcessor<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, LockOrderDAGFactory.TradeLockNodeResult> {

    @Resource
    private ITradeRepository repository;

    @Override
    public LockOrderDAGFactory.TradeLockNodeResult process(TradeLockRequestEntity requestParam, LockOrderDAGFactory.TradeLockContext readonlyContext) throws Exception {
        log.info("DAG-Lock: 加载锁单商品的活动信息 activityId:{}", requestParam.getActivityId());
        GroupBuyActivityEntity activity = repository.queryGroupBuyActivityEntityByActivityId(requestParam.getActivityId());
        return new LockOrderDAGFactory.TradeLockNodeResult(LockOrderDAGFactory.TradeLockNodeResult.TYPE_ACTIVITY, activity);
    }
}
