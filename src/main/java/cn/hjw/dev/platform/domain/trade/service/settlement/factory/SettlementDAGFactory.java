package cn.hjw.dev.platform.domain.trade.service.settlement.factory;

import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradePaySettlementEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeSettlementRequest;
import cn.hjw.dev.platform.domain.trade.service.settlement.processors.*;
import cn.hjw.dev.platform.domain.trade.service.settlement.strategy.SettlementTerminalStrategy;
import cn.hjw.dev.platform.domain.trade.service.settlement.strategy.SettlementUpdateStrategy;
import cn.hjw.dev.dagflow.ExecutableGraph;
import cn.hjw.dev.dagflow.config.GraphConfig;
import cn.hjw.dev.dagflow.engine.DAGEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

@Configuration
public class SettlementDAGFactory {

    // 注入 DAG 专用的大池子
    @Resource(name = "dagExecutor")
    private ExecutorService dagExecutor;

    @Bean("tradeSettlementDAGEngine")
    public ExecutableGraph<TradeSettlementRequest, TradeSettlementContext, TradePaySettlementResult> tradeSettlementDAGEngine(
            CheckBlacklistProcessor checkBlacklist,
            LoadPayOrderProcessor loadPayOrder,
            ValidateTeamProcessor validateTeam,
            IntegrityCheckProcessor integrityCheck,
            SettlementPersistProcessor settlementPersist,
            SettlementUpdateStrategy updateStrategy,
            SettlementTerminalStrategy terminalStrategy
    ) {
        // 1. 显式泛型定义 <Request, Context, NodeResult, FinalResult>
        GraphConfig<TradeSettlementRequest,
                TradeSettlementContext,
                TradeSettlementNodeResult,
                TradePaySettlementResult> config
                = new GraphConfig<>(dagExecutor);

        // 2. 注册节点
        config.addNode("checkBlacklist", checkBlacklist)
                .addNode("loadPayOrder", loadPayOrder)
                .addNode("validateTeam", validateTeam)
                .addNode("integrityCheck", integrityCheck)
                .addNode("settlementPersist", settlementPersist);

        // 3. 编排依赖

        // Level 0: 并行节点，互不依赖 loadPayOrder,checkBlacklist

        // Level 1: 必须等 LoadPayOrder 拿到 teamId 后才能查 team，才能做资金校验
        config.addRoute("loadPayOrder", "validateTeam");
        config.addRoute("loadPayOrder", "integrityCheck");

        // Level 2: 必须等所有校验通过，才做落库
        config.addRoute("validateTeam", "settlementPersist");
        config.addRoute("integrityCheck", "settlementPersist");
        // 黑名单校验虽然并行，但如果它抛异常，全流程熔断

        // 4. 策略
        config.setUpdateStrategy(updateStrategy);
        config.setTerminalStrategy(terminalStrategy);

        return new DAGEngine<>(config);
    }


    // --- 内部类定义上下文与结果 ---
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TradeSettlementContext {
        private MarketPayOrderEntity payOrder;
        private GroupBuyTeamEntity team;
        private TradePaySettlementResult finalResult;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TradeSettlementNodeResult {
        private String type;
        private Object data;

        public static final String TYPE_BLACKLIST = "BLACKLIST";
        public static final String TYPE_ORDER = "ORDER";
        public static final String TYPE_TEAM = "TEAM";
        public static final String TYPE_INTEGRITY = "INTEGRITY";
        public static final String TYPE_PERSIST = "PERSIST";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TradePaySettlementResult {
        private boolean isCompleted;
        private TradePaySettlementEntity settlementEntity;

    }

}
