package cn.hjw.dev.platform.domain.trade.service.lock.processors;

import cn.hjw.dev.platform.domain.activity.IGroupBuyMarketService;
import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;

import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class CallTrialProcessor implements DAGNodeProcessor<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, LockOrderDAGFactory.TradeLockNodeResult> {

    @Resource
    private IGroupBuyMarketService iGroupBuyMarketService;

    @Override
    public LockOrderDAGFactory.TradeLockNodeResult process(TradeLockRequestEntity req, LockOrderDAGFactory.TradeLockContext ctx) throws Exception {
        log.info("DAG-Lock: 调用营销试算服务 (Nested DAG)");

        // 1. 参数转换: TradeLockRequest -> MarketProductEntity
        MarketProductEntity trialReq = MarketProductEntity.builder()
                .userId(req.getUserId())
                .activityId(req.getActivityId())
                .goodsId(req.getGoodsId())
                .source(req.getSource())
                .channel(req.getChannel())
                .build();

        // 2. 调用试算接口 (这里会触发 Trial DAG 的执行)
        TrialBalanceEntity trialResult = iGroupBuyMarketService.indexMarketTrial(trialReq);

        // 3. 校验试算结果
        // 如果 Trial DAG 认为不可见或不可买，这里拦截
        if (trialResult == null || !Boolean.TRUE.equals(trialResult.getIsEnable())) {
            throw new AppException(ResponseCode.E0101.getCode(), "商品不可购买或活动不满足条件");
        }

        return new LockOrderDAGFactory.TradeLockNodeResult(LockOrderDAGFactory.TradeLockNodeResult.TYPE_TRIAL, trialResult);
    }
}
