package cn.hjw.dev.platform.domain.activity;

import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;

/**
 * 定义了首页营销的标准行为
 */
public interface IGroupBuyMarketService {

    /**
     * 首页营销试算
     * @param marketProductEntity 营销商品实体
     * @return 试算结果实体
     * @throws Exception 可能抛出的异常
     */
    TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception;
}
