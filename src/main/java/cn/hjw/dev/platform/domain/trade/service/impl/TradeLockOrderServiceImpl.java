package cn.hjw.dev.platform.domain.trade.service.impl;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.*;
import cn.hjw.dev.platform.domain.trade.service.ITradeLockOrderService;
import cn.hjw.dev.platform.domain.trade.service.lock.factory.LockOrderDAGFactory;
import cn.hjw.dev.dagflow.ExecutableGraph;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 交易订单服务
 * @create 2025-01-11 08:07
 */
@Slf4j
@Service
public class TradeLockOrderServiceImpl implements ITradeLockOrderService {

    @Resource(name = "tradeLockDAGEngine")
    private ExecutableGraph<TradeLockRequestEntity, LockOrderDAGFactory.TradeLockContext, MarketPayOrderEntity> tradeLockDAGEngine;

    @Resource
    private ITradeRepository repository;

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        log.info("拼团交易-根据外部单号查询未支付营销订单:{} outTradeNo:{}", userId, outTradeNo);
        return repository.queryMarketPayOrderEntityByOutTradeNo(userId, outTradeNo);
    }

    @Override
    public MarketPayOrderEntity lockMarketPayOrder(TradeLockRequestEntity req) throws Exception {

        if(StringUtils.isAnyBlank(req.getUserId(),req.getChannel(),req.getSource(),req.getGoodsId(),req.getOutTradeNo()) && ObjectUtils.isEmpty(req.getActivityId())) {
            throw new IllegalArgumentException("锁定营销预支付订单，必要参数不能为空");
        }
        log.info("拼团交易-锁定营销预支付订单:{}", req.getOutTradeNo());

        // 2. 构建 DAG 上下文
        LockOrderDAGFactory.TradeLockContext context = new LockOrderDAGFactory.TradeLockContext();

        // 3. 执行 DAG
        return tradeLockDAGEngine.apply(req, context);
    }

}
