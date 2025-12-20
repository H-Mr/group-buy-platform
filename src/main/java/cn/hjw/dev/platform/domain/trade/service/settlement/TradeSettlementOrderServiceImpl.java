package cn.hjw.dev.platform.domain.trade.service.settlement;

import cn.hjw.dev.platform.domain.trade.event.GroupBuyCompletedEventTypeType;
import cn.hjw.dev.platform.domain.trade.model.entity.*;
import cn.hjw.dev.platform.domain.trade.service.ITradeSettlementOrderService;
import cn.hjw.dev.platform.domain.trade.service.settlement.factory.SettlementDAGFactory;
import cn.hjw.dev.dagflow.ExecutableGraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Slf4j
@Service
public class TradeSettlementOrderServiceImpl implements ITradeSettlementOrderService {


    @Resource(name = "tradeSettlementDAGEngine")
    private ExecutableGraph<TradeSettlementRequest,
            SettlementDAGFactory.TradeSettlementContext,
            SettlementDAGFactory.TradePaySettlementResult> tradeSettlementDAGEngine;

    @Resource
    private GroupBuyCompletedEventTypeType groupBuyCompletedEventType; // 注入组队完成事件

    /**
     * 拼团交易-支付订单结算
     *
     * @param tradePaySuccessEntity 支付成功实体对象
     * @return 交易支付结算实体对象
     */
    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception {
        log.info("拼团交易-支付订单结算-userID:{} outTradeNo:{}", tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());

        // 1. 构建请求
        TradeSettlementRequest request = TradeSettlementRequest.builder()
                .userId(tradePaySuccessEntity.getUserId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .build();

        // 2. 构建上下文
        SettlementDAGFactory.TradeSettlementContext context = new SettlementDAGFactory.TradeSettlementContext();

        SettlementDAGFactory.TradePaySettlementResult settlementResult = tradeSettlementDAGEngine.apply(request, context);
        TradePaySettlementEntity settlementEntity = settlementResult.getSettlementEntity();
        boolean completed = settlementResult.isCompleted();
        // 组队回调处理 - 处理失败也会有定时任务补偿，通过这样的方式，可以减轻任务调度，提高时效性
//        if(completed) {
//            // 发送包含 teamId 的事件，触发 "单条回调"
//            groupBuyCompletedEventType.publishGroupBuyCompleted(settlementEntity.getTeamId());
//        }

        // 5. 返回结算信息 - 公司中开发这样的流程时候，会根据外部需要进行值的设置
        return TradePaySettlementEntity.builder()
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .userId(tradePaySuccessEntity.getUserId())
                .teamId(settlementEntity.getTeamId())
                .activityId(settlementEntity.getActivityId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .build();
    }

}
