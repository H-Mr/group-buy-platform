package cn.hjw.dev.platform.domain.activity.service.trial.processor;

import cn.hjw.dev.platform.domain.activity.adapter.repository.IActivityRepository;
import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.model.valobj.SCSkuActivityVO;
import cn.hjw.dev.platform.domain.activity.service.trial.factory.MarketDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class LoadTrialActivityProcessor implements DAGNodeProcessor<MarketProductEntity, MarketDAGFactory.TrialContext, MarketDAGFactory.TrialNodeResult> {

    @Resource
    private IActivityRepository activityRepository; // 注入活动仓库，用于加载活动数据

    /**
     * 加载试算的活动配置
     * @param requestParam 传入流程图的参数
     * @param readonlyContext 只读上下文
     * @return
     * @throws Exception
     */
    @Override
    public MarketDAGFactory.TrialNodeResult process(MarketProductEntity requestParam, MarketDAGFactory.TrialContext readonlyContext) throws Exception {
        log.info("DAG节点-根据商品信息加载活动配置: goodsId:{}", requestParam.getGoodsId());
        SCSkuActivityVO scSkuActivityVO = activityRepository.querySCSkuActivityBySCGoodsId(
                requestParam.getSource(), requestParam.getChannel(), requestParam.getGoodsId()
        );
        // 当前商品ID没有对应的活动配置，直接返回活动为空
        if(ObjectUtils.isEmpty(scSkuActivityVO)) {
            return new MarketDAGFactory.TrialNodeResult(MarketDAGFactory.TrialNodeResult.TYPE_ACTIVITY, null);
        }
        // 根据活动ID查询活动配置信息
        GroupBuyActivityDiscountVO activityVo = activityRepository.queryGroupBuyActivityDiscountVO(scSkuActivityVO.getActivityId());
        return new MarketDAGFactory.TrialNodeResult(MarketDAGFactory.TrialNodeResult.TYPE_ACTIVITY, activityVo);
    }
}
