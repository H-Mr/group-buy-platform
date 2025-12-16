package cn.hjw.dev.platform.domain.trade.service.lock.processors;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class LoadUserCountProcessor implements DAGNodeProcessor<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, LockOrderDAGFactory.TradeLockNodeResult> {

    @Resource
    private ITradeRepository repository;

    @Override
    public LockOrderDAGFactory.TradeLockNodeResult process(TradeLockRequestEntity req, LockOrderDAGFactory.TradeLockContext ctx) {
        log.info("DAG-Lock: 加载当前用户在当前活动下单的次数 userId:{}; activityId：{}", req.getUserId(), req.getActivityId());
        Integer count = repository.queryOrderCountByActivityId(req.getActivityId(), req.getUserId());
        return new LockOrderDAGFactory.TradeLockNodeResult(LockOrderDAGFactory.TradeLockNodeResult.TYPE_USER_COUNT, count);
    }
}
