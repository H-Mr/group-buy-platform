package cn.hjw.dev.platform.domain.trade.service.lock.factory;

import cn.hjw.dev.dagflow.engine.DAGEngine;
import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradeLockRequestEntity;
import cn.hjw.dev.platform.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.hjw.dev.platform.domain.trade.service.lock.processors.*;
import cn.hjw.dev.platform.domain.trade.service.lock.strategy.TradeLockTerminalStrategy;
import cn.hjw.dev.platform.domain.trade.service.lock.strategy.TradeLockUpdateStrategy;
import cn.hjw.dev.dagflow.ExecutableGraph;
import cn.hjw.dev.dagflow.config.GraphConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

@Configuration
public class LockOrderDAGFactory {

    // 注入 DAG 专用的大池子
    @Resource(name = "dagExecutor")
    private ExecutorService dagExecutor;

    @Bean("tradeLockDAGEngine")
    public ExecutableGraph<TradeLockRequestEntity, TradeLockContext, MarketPayOrderEntity> tradeLockDAGEngine(
            LoadLockActivityProcessor loadActivity,
            LoadUserCountProcessor loadUserCount,
            LoadActivityProgressProcessor loadActivityProgress,
            RiskControlProcessor riskControl,
            TradeCheckProcessor checkProcessor,
            CallTrialProcessor callTrial,
            LockPersistProcessor lockPersist,
            TradeLockUpdateStrategy updateStrategy,
            TradeLockTerminalStrategy terminalStrategy
    ) {
        // 显式指定 4 个泛型：Request, Context, NodeResult, FinalResult
        GraphConfig<TradeLockRequestEntity,
                TradeLockContext,
                TradeLockNodeResult,
                MarketPayOrderEntity> config = new GraphConfig<>(dagExecutor);

        // 1. 注册节点
        config.addNode("loadActivity", loadActivity)
                .addNode("loadUserCount", loadUserCount)
                .addNode("riskControl", riskControl)
                .addNode("check", checkProcessor)
                .addNode("callTrial", callTrial)
                .addNode("loadActivityProgress", loadActivityProgress)
                .addNode("lockPersist", lockPersist);


        // 2. 编排依赖

        // Phase 1: 并行加载 -> 统一校验
        config.addRoute("loadActivityProgress", "check");
        config.addRoute("loadActivity", "check");
        config.addRoute("loadUserCount", "check");
        config.addRoute("riskControl", "check");

        // Phase 2: 校验通过 -> 调用试算 (Small DAG)
        config.addRoute("check", "callTrial");

        // Phase 3: 试算成功 -> 落库锁定
        config.addRoute("callTrial", "lockPersist");

        // 3. 配置策略
        config.setUpdateStrategy(updateStrategy);
        config.setTerminalStrategy(terminalStrategy);

        // 4. 构建引擎
        return new DAGEngine<>(config);
    }


    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class TradeLockContext {

        // 中间数据：活动信息
        private GroupBuyActivityEntity activity;

        // 中间数据：拼团进度
        private GroupBuyProgressVO groupBuyProgress;

        // 中间数据：用户已购次数
        private Integer userTakeCount;

        // 中间数据：风控评分
        private Integer riskScore;

        // 【关键】接收“小 DAG”试算的结果
        private TrialBalanceEntity trialResult;

        // 最终产出：落库后的订单实体
        private MarketPayOrderEntity lockedOrder;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TradeLockNodeResult {
        private String type;
        private Object data;

        public static final String TYPE_ACTIVITY = "ACTIVITY";
        public static final String TYPE_USER_COUNT = "USER_COUNT";
        public static final String TYPE_GROUP_BUY_PROGRESS = "GROUP_BUY_PROGRESS";
        public static final String TYPE_RISK = "RISK";
        public static final String TYPE_CHECK = "CHECK";
        public static final String TYPE_TRIAL = "TRIAL";
        public static final String TYPE_PERSIST = "PERSIST";
    }
}
