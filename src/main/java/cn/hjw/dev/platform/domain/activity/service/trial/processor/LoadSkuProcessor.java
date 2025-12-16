package cn.hjw.dev.platform.domain.activity.service.trial.processor;

import cn.hjw.dev.platform.domain.activity.adapter.repository.IActivityRepository;
import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.valobj.SkuVO;
import cn.hjw.dev.platform.domain.activity.service.trial.factory.MarketDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 加载商品SKU信息节点
 */
@Slf4j
@Component
public class LoadSkuProcessor implements DAGNodeProcessor<MarketProductEntity, MarketDAGFactory.TrialContext, MarketDAGFactory.TrialNodeResult> {

    @Resource
    private IActivityRepository activityRepository;

    @Override
    public MarketDAGFactory.TrialNodeResult process(MarketProductEntity request, MarketDAGFactory.TrialContext readonlyContext) throws Exception {
        log.info("DAG节点-加载当前试算的商品信息: goodsId:{}", request.getGoodsId());
        SkuVO skuVO = activityRepository.querySkuByGoodsId(request.getGoodsId());
        return new MarketDAGFactory.TrialNodeResult(MarketDAGFactory.TrialNodeResult.TYPE_SKU, skuVO);
    }
}
