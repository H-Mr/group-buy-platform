package cn.hjw.dev.platform.domain.activity.service.trial.processor;

import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.model.valobj.SkuVO;
import cn.hjw.dev.platform.domain.activity.service.discountStrategy.IDiscountCalculateService;
import cn.hjw.dev.platform.domain.activity.service.trial.factory.MarketDAGFactory;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 对应原 MarketNode 的核心计算逻辑。注意：此节点依赖上述加载节点完成，但 DAG 引擎会自动处理依赖，我们在代码里直接从 Context 取值即可。
 */
@Slf4j
@Component
public class CalculatePriceProcessor implements DAGNodeProcessor<MarketProductEntity, MarketDAGFactory.TrialContext, MarketDAGFactory.TrialNodeResult> {

    @Resource
    private Map<String, IDiscountCalculateService> discountCalculateServiceMap;

    @Override
    public MarketDAGFactory.TrialNodeResult process(MarketProductEntity request, MarketDAGFactory.TrialContext context) throws Exception {
        // 从 Context 获取 L0 层加载的数据
        GroupBuyActivityDiscountVO activityVO = context.getActivityConfig();
        SkuVO skuVO = context.getSkuInfo();

        // 校验数据完整性 (原 MarketNode 的校验逻辑)
        if (!ObjectUtils.allNotNull(activityVO, skuVO)) {
            log.warn("DAG节点-价格计算: 数据缺失，无法计算");
            // 可以选择抛出异常或返回特定标识，这里演示抛异常中断
            throw new AppException(ResponseCode.ILLEGAL_MARKET_CONFIG.getCode(), "试算数据缺失");
        }

        log.info("DAG节点-营销商品价格计算: plan:{}", activityVO.getGroupBuyDiscount().getMarketPlan());

        IDiscountCalculateService service = discountCalculateServiceMap.get(activityVO.getGroupBuyDiscount().getMarketPlan());
        if (ObjectUtils.isEmpty(service)) {
            throw new AppException(ResponseCode.ILLEGAL_DISCOUNT_SERVICE.getCode(), ResponseCode.ILLEGAL_DISCOUNT_SERVICE.getInfo());
        }

        BigDecimal payPrice = service.calculateDiscountPrice(
                request.getUserId(),
                skuVO.getOriginalPrice(),
                activityVO.getGroupBuyDiscount()
        );

        return new MarketDAGFactory.TrialNodeResult(MarketDAGFactory.TrialNodeResult.TYPE_CALC_PRICE, payPrice);
    }
}
