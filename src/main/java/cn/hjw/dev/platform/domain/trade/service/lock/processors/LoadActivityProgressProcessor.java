package cn.hjw.dev.platform.domain.trade.service.lock.processors;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class LoadActivityProgressProcessor implements DAGNodeProcessor<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, LockOrderDAGFactory.TradeLockNodeResult> {

    @Resource
    private ITradeRepository repository;

    @Override
    public LockOrderDAGFactory.TradeLockNodeResult process(TradeLockRequestEntity requestParam, LockOrderDAGFactory.TradeLockContext readonlyContext) throws Exception {
        String teamId = requestParam.getTeamId();
        log.info("DAG-Lock: 加载锁单的队进度 activityId:{}", requestParam.getActivityId());
        if (StringUtils.isAnyBlank(teamId)) {
            return new LockOrderDAGFactory.TradeLockNodeResult(LockOrderDAGFactory.TradeLockNodeResult.TYPE_GROUP_BUY_PROGRESS,null);
        }
        GroupBuyProgressVO groupBuyProgressVO = repository.queryGroupBuyProgress(teamId);
        return new LockOrderDAGFactory.TradeLockNodeResult(LockOrderDAGFactory.TradeLockNodeResult.TYPE_GROUP_BUY_PROGRESS,groupBuyProgressVO);
    }
}
