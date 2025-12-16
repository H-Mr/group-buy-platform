package cn.hjw.dev.platform.domain.activity.service.trial.processor;

import cn.hjw.dev.platform.domain.activity.adapter.repository.IActivityRepository;
import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.model.valobj.TagScopeEnum;
import cn.hjw.dev.platform.domain.activity.service.trial.factory.MarketDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
public class CrowdTagProcessor implements DAGNodeProcessor<MarketProductEntity, MarketDAGFactory.TrialContext, MarketDAGFactory.TrialNodeResult> {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public MarketDAGFactory.TrialNodeResult process(MarketProductEntity request, MarketDAGFactory.TrialContext context) {
        GroupBuyActivityDiscountVO activityVO = context.getActivityConfig();
        if (activityVO == null) return null; // 容错

        boolean visible = activityVO.isVisible();
        boolean enable = activityVO.isEnable();

        // 如果没有配置标签范围，默认全部通过
        if(StringUtils.isBlank(activityVO.getTagScope()))  {
            return new MarketDAGFactory.TrialNodeResult(MarketDAGFactory.TrialNodeResult.TYPE_CROWD_TAG, new MarketDAGFactory.TagResult(TagScopeEnum.VISIBLE.getAllow(), TagScopeEnum.ENABLE.getAllow()));
        }

        boolean isWithin = activityRepository.isTargetCrowd(activityVO.getTagId(), request.getUserId());
        // 当前没有启用标签，所以全部都放行。
        return new MarketDAGFactory.TrialNodeResult(MarketDAGFactory.TrialNodeResult.TYPE_CROWD_TAG,
                new MarketDAGFactory.TagResult(true,true));
    }

}
