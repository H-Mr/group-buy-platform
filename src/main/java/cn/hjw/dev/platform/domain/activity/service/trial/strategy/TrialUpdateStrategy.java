package cn.hjw.dev.platform.domain.activity.service.trial.strategy;

import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.model.valobj.SkuVO;
import cn.hjw.dev.platform.domain.activity.service.trial.factory.MarketDAGFactory;
import cn.hjw.dev.dagflow.processor.UpdateStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TrialUpdateStrategy implements UpdateStrategy<MarketProductEntity, MarketDAGFactory.TrialContext, MarketDAGFactory.TrialNodeResult> {
    @Override
    public void updateContext(MarketProductEntity request, MarketDAGFactory.TrialContext context, MarketDAGFactory.TrialNodeResult result) {
        if (result == null) return;

        switch (result.getType()) {
            case MarketDAGFactory.TrialNodeResult.TYPE_ACTIVITY:
                context.setActivityConfig((GroupBuyActivityDiscountVO) result.getData());
                break;
            case MarketDAGFactory.TrialNodeResult.TYPE_SKU:
                context.setSkuInfo((SkuVO) result.getData());
                break;
            case MarketDAGFactory.TrialNodeResult.TYPE_CALC_PRICE:
                BigDecimal payPrice = (BigDecimal) result.getData();
                context.setPayPrice(payPrice);
                // 联动更新抵扣金额
                if (context.getSkuInfo() != null) {
                    context.setDeductionPrice(context.getSkuInfo().getOriginalPrice().subtract(payPrice));
                }
                break;
            case MarketDAGFactory.TrialNodeResult.TYPE_CROWD_TAG:
                MarketDAGFactory.TagResult tagRes = (MarketDAGFactory.TagResult) result.getData();
                context.setVisible(tagRes.isVisible());
                context.setEnable(tagRes.isEnable());
                break;
            case MarketDAGFactory.TrialNodeResult.TYPE_SWITCH_CHECK:
                // 开关检查通过，无需特别更新，失败会抛异常
                break;
            default:
                break;
        }
    }
}
