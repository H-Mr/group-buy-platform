package cn.hjw.dev.mall.types.framework.chain.iterate;

import cn.hjw.dev.types.framework.graph.ExecutableGraph;
import cn.hjw.dev.types.framework.graph.GraphEngine;
import cn.hjw.dev.types.framework.graph.config.GraphConfig;
import cn.hjw.dev.types.framework.graph.processor.INodeProcessor;
import cn.hjw.dev.types.framework.graph.signal.StandardSignal;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 责任链引擎（基于图引擎的封装）
 * 作用：屏蔽图引擎的复杂配置，提供傻瓜式的线性链条构建能力
 */
public class IterateChainEngine<T,C,R> implements ExecutableGraph<T, C, R> {

    // 暂存处理器列表，顺序即为执行顺序
    private final List<INodeProcessor<T, C>> processors = new ArrayList<>();

    // 终结策略
    private BiFunction<T, C, R> terminalStrategy;

    // 懒加载构建好的图引擎
    //ChainEngine 对象本身通常是单例的（Spring Bean），但它的内部属性 graphEngine 是懒加载（Lazy Initialization）的。
    // **“单例对象”不代表其“内部状态”自动线程安全**。如果不加锁，在高并发的“瞬间启动”场景下，会发生竞态条件（Race Condition）。
    private GraphEngine<T, C, R> graphEngine;

    private IterateChainEngine() {} // 私有构造函数，禁止外部直接 new


    /**
     * 静态构建方法
     */
    public static <T,C,R> IterateChainEngine<T,C,R> builder() {
        return new IterateChainEngine<>();
    }
    /**
     * 添加处理器（链式调用）
     */
    public IterateChainEngine<T, C, R> addProcessor(INodeProcessor<T, C> processor) {
        this.processors.add(processor);
        return this;
    }

    /**
     * 设置终结策略（链式调用）
     */
    public IterateChainEngine<T, C, R> terminalStrategy(BiFunction<T, C, R> terminalStrategy) {
        this.terminalStrategy = terminalStrategy;
        return this;
    }

    /**
     * 显式构建方法：必须在配置完成后，执行前调用一次
     */
    public IterateChainEngine<T, C, R> build() {
        this.graphEngine = buildGraphEngine();
        return this;
    }

    @Override
    public R apply(T request, C context) throws Exception {
        // 如果没 build 就跑，直接抛错，而不是偷偷帮你 build
        if (this.graphEngine == null) {
            throw new IllegalStateException("ChainEngine not built! Please call build() first.");
        }
        return graphEngine.apply(request, context);
    }

    /**
     * 核心逻辑：将线性列表转化为图配置
     */
    private GraphEngine<T, C, R> buildGraphEngine() {
        if (processors.isEmpty()) {
            throw new IllegalStateException("Chain is empty! Please add processors.");
        }

        GraphConfig<T, C, R> config = new GraphConfig<>();
        config.setNodeProcessorTable(new HashMap<>());

        // 1. 设置终结策略
        config.setTerminalStrategy(terminalStrategy);

        // 2. 自动生成节点ID并连线
        String previousNodeId = null;

        for (int i = 0; i < processors.size(); i++) {
            // 自动生成 ID: node_0, node_1 ...
            String currentNodeId = "chain_node_" + i;
            INodeProcessor<T, C> processor = processors.get(i);

            // 注册节点
            config.getNodeProcessorTable().put(currentNodeId, processor);

            // 设置入口 (如果是第一个节点)
            if (i == 0) {
                config.setEntryPoint(currentNodeId);
            }

            // 建立连接：前一个节点 + NEXT -> 当前节点
            if (ObjectUtils.isNotEmpty(previousNodeId)) {
                config.addRoute(previousNodeId, StandardSignal.NEXT, currentNodeId);
            }
            previousNodeId = currentNodeId;
        }

        // 最后一个节点不需要配置 NEXT 路由，图引擎查不到路由会自动终止 -> 执行 TerminalStrategy
        return new GraphEngine<>(config);
    }
}
