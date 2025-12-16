package cn.hjw.dev.platform.domain.activity.service.trial.strategy;

import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;
import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.model.valobj.SkuVO;
import cn.hjw.dev.platform.domain.activity.service.trial.factory.MarketDAGFactory;
import cn.hjw.dev.dagflow.processor.TerminalStrategy;
import org.springframework.stereotype.Component;

@Component
public class TrialTerminalStrategy implements TerminalStrategy<MarketProductEntity, MarketDAGFactory.TrialContext, TrialBalanceEntity> {
    @Override
    public TrialBalanceEntity terminate(MarketProductEntity requestParam, MarketDAGFactory.TrialContext context) throws Exception {
        GroupBuyActivityDiscountVO activity = context.getActivityConfig();
        SkuVO sku = context.getSkuInfo();

        // 如果核心数据缺失（比如活动不存在），返回空对象或抛异常
        if (activity == null || sku == null) {
            return TrialBalanceEntity.builder().build();
        }

        return TrialBalanceEntity.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .deductionPrice(context.getDeductionPrice())
                .payPrice(context.getPayPrice())
                .targetCount(activity.getTarget())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .isVisible(context.isVisible())
                .isEnable(context.isEnable())
                .groupBuyActivityDiscountVO(activity)
                .build();
    }
}
