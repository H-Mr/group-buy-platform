package cn.hjw.dev.platform.domain.trade.service.lock.strategy;

import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.dagflow.processor.UpdateStrategy;
import org.springframework.stereotype.Component;

@Component
public class TradeLockUpdateStrategy implements UpdateStrategy<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, LockOrderDAGFactory.TradeLockNodeResult> {

    @Override
    public void updateContext(TradeLockRequestEntity req, LockOrderDAGFactory.TradeLockContext ctx, LockOrderDAGFactory.TradeLockNodeResult res) {
        if (res == null) return;
        switch (res.getType()) {
            case LockOrderDAGFactory.TradeLockNodeResult.TYPE_ACTIVITY:
                ctx.setActivity((GroupBuyActivityEntity) res.getData());
                break;
            case LockOrderDAGFactory.TradeLockNodeResult.TYPE_USER_COUNT:
                ctx.setUserTakeCount((Integer) res.getData());
                break;
            case LockOrderDAGFactory.TradeLockNodeResult.TYPE_GROUP_BUY_PROGRESS:
                ctx.setGroupBuyProgress((GroupBuyProgressVO) res.getData());
                break;
            case LockOrderDAGFactory.TradeLockNodeResult.TYPE_RISK:
                ctx.setRiskScore((Integer) res.getData());
                break;
            case LockOrderDAGFactory.TradeLockNodeResult.TYPE_TRIAL:
                ctx.setTrialResult((TrialBalanceEntity) res.getData());
                break;
            case LockOrderDAGFactory.TradeLockNodeResult.TYPE_PERSIST:
                ctx.setLockedOrder((MarketPayOrderEntity) res.getData());
                break;
        }
    }
}
