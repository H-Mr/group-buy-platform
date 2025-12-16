package cn.hjw.dev.platform.domain.trade.service.settlement.processors;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeSettlementRequest;
import cn.hjw.dev.platform.domain.trade.service.settlement.factory.SettlementDAGFactory;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 黑名单校验处理器
 */
@Slf4j
@Component
public class CheckBlacklistProcessor implements DAGNodeProcessor<TradeSettlementRequest, SettlementDAGFactory.TradeSettlementContext, SettlementDAGFactory.TradeSettlementNodeResult> {

    @Resource
    private ITradeRepository repository;

    @Override
    public SettlementDAGFactory.TradeSettlementNodeResult process(TradeSettlementRequest req, SettlementDAGFactory.TradeSettlementContext ctx) {
        log.info("DAG-Settlement: 结算渠道黑名单校验 source:{} channel:{}", req.getSource(), req.getChannel());
        if (repository.isSCBlackIntercept(req.getSource(), req.getChannel())) {
            log.warn("渠道黑名单拦截: {} {}", req.getSource(), req.getChannel());
            throw new AppException(ResponseCode.E0105);
        }
        return new SettlementDAGFactory.TradeSettlementNodeResult(SettlementDAGFactory.TradeSettlementNodeResult.TYPE_BLACKLIST, true);
    }
}
