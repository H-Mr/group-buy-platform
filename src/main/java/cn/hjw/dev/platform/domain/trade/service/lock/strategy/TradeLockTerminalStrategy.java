package cn.hjw.dev.platform.domain.trade.service.lock.strategy;

import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.dagflow.processor.TerminalStrategy;
import org.springframework.stereotype.Component;

@Component
public class TradeLockTerminalStrategy implements TerminalStrategy<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, MarketPayOrderEntity> {

    @Override
    public MarketPayOrderEntity terminate(TradeLockRequestEntity req, LockOrderDAGFactory.TradeLockContext ctx) {
        // 直接返回落库后的订单实体
        return ctx.getLockedOrder();
    }
}
