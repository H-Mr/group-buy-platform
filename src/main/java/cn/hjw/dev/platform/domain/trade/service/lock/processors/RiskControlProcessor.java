package cn.hjw.dev.platform.domain.trade.service.lock.processors;

import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RiskControlProcessor implements DAGNodeProcessor<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, LockOrderDAGFactory.TradeLockNodeResult> {

    @Override
    public LockOrderDAGFactory.TradeLockNodeResult process(TradeLockRequestEntity req, LockOrderDAGFactory.TradeLockContext ctx) {
        log.info("DAG-Lock: 调用风控服务,查询当前用户的风险等级 userId:{}", req.getUserId());
        // 模拟逻辑：如果ID以 'risk' 开头，分数为 90 (高风险)，否则 0
        int score = req.getUserId().startsWith("risk") ? 90 : 0;
        return new LockOrderDAGFactory.TradeLockNodeResult(LockOrderDAGFactory.TradeLockNodeResult.TYPE_RISK, score);
    }
}
