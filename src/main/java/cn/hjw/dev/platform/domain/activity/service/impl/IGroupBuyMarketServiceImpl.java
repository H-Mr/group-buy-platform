package cn.hjw.dev.platform.domain.activity.service.impl;

import cn.hjw.dev.platform.domain.activity.IGroupBuyMarketService;
import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;
import cn.hjw.dev.platform.domain.activity.service.trial.factory.MarketDAGFactory;
import cn.hjw.dev.dagflow.ExecutableGraph;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 首页营销服务
 * @create 2024-12-14 14:33
 */
@Service
public class IGroupBuyMarketServiceImpl implements IGroupBuyMarketService {

    @Resource(name = "trialBalanceDAGEngine")
    private ExecutableGraph<MarketProductEntity, MarketDAGFactory.TrialContext, TrialBalanceEntity> trialBalanceDAGEngine;

    @Override
    public TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception {

        // 1. 创建 Context (使用 MarketDAGFactory 中的静态内部类)
        MarketDAGFactory.TrialContext context = new MarketDAGFactory.TrialContext();

        return trialBalanceDAGEngine.apply(marketProductEntity,context);
    }
}
