package cn.hjw.dev.platform.domain.activity.service.trial.factory;

import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;
import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.model.valobj.SkuVO;
import cn.hjw.dev.platform.domain.activity.service.trial.processor.*;
import cn.hjw.dev.platform.domain.activity.service.trial.strategy.TrialTerminalStrategy;
import cn.hjw.dev.platform.domain.activity.service.trial.strategy.TrialUpdateStrategy;
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
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;

@Configuration
public class MarketDAGFactory {

    // 注入 DAG 专用的大池子
    @Resource(name ="dagExecutor")
    private ExecutorService dagExecutor;
    @Bean("trialBalanceDAGEngine")
    public ExecutableGraph<MarketProductEntity, TrialContext, TrialBalanceEntity> trialBalanceDAGEngine(
            LoadTrialActivityProcessor loadTrialActivityProcessor, // 活动加载处理器
            LoadSkuProcessor loadSkuProcessor, // 商品 SKU 加载处理器
            SwitchCheckProcessor switchCheckProcessor, // 开关校验处理器
            CalculatePriceProcessor calculatePriceProcessor, // 价格计算处理器
            CrowdTagProcessor crowdTagProcessor, // 人群标签处理器
            TrialUpdateStrategy updateStrategy, // 自定义的更新策略
            TrialTerminalStrategy terminalStrategy // 自定义的终止策略
    ) {
        GraphConfig<MarketProductEntity, TrialContext, TrialNodeResult, TrialBalanceEntity> config
                = new GraphConfig<>(dagExecutor);

        // 1. 注册所有节点
        config.addNode("loadActivity", loadTrialActivityProcessor)
                .addNode("loadSku", loadSkuProcessor)
                .addNode("switchCheck", switchCheckProcessor)
                .addNode("calcPrice", calculatePriceProcessor)
                .addNode("crowdTag", crowdTagProcessor);

        // 2. 定义 DAG 结构

        // Level 1: 计算依赖数据加载,让 SwitchCheck 也成为 calcPrice 的前置，确保开关不通过时直接熔断，不浪费计算资源
        // Level 2 计算优惠
        config.addRoute("loadActivity", "calcPrice");
        config.addRoute("loadSku", "calcPrice");
        config.addRoute("switchCheck", "calcPrice");

        // level2: 人群标签校验和价格计算并行
        config.addRoute("loadActivity", "crowdTag");

        // 3. 配置策略
        config.setUpdateStrategy(updateStrategy);
        config.setTerminalStrategy(terminalStrategy);

        // 4. 构建引擎
        return new DAGEngine<>(config);
    }

/**
 * 试算流程上下文 (Context)
 * 用于在 DAG 节点之间传递数据
 */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrialContext {
    // 活动配置信息
    private GroupBuyActivityDiscountVO activityConfig;
    // 商品 SKU 信息
    private SkuVO skuInfo;
    // 原始价格
    private BigDecimal originalPrice;
    // 支付金额 (计算后)
    private BigDecimal payPrice;
    // 抵扣金额
    private BigDecimal deductionPrice;
    // 是否可见 (人群标签/配置决定)
    private boolean visible = true;
    // 是否可参与 (人群标签/配置决定)
    private boolean enable = true;
    // 流程是否因异常/校验失败而中断
    private boolean terminated = false;
    private String terminateReason;
    }

/**
 * 试算节点返回结果 (Result)
 * 统一封装所有节点的返回值
 */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrialNodeResult {
        private String type;
        private Object data;

    // 定义返回类型常量，方便 UpdateStrategy 识别
    public static final String TYPE_ACTIVITY = "ACTIVITY"; // 活动配置
    public static final String TYPE_SKU = "SKU"; // 商品 SKU 信息
    public static final String TYPE_SWITCH_CHECK = "SWITCH_CHECK"; // 开关校验结果
    public static final String TYPE_CALC_PRICE = "CALC_PRICE"; // 价格计算结果
    public static final String TYPE_CROWD_TAG = "CROWD_TAG"; // 人群标签校验结果
    }

    @Data
    @AllArgsConstructor
    public static class TagResult {
        private boolean visible;
        private boolean enable;
    }
}
