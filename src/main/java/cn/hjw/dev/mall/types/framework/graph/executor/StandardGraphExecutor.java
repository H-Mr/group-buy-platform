package cn.hjw.dev.mall.types.framework.graph.executor;

import cn.hjw.dev.types.framework.graph.config.GraphConfig;
import cn.hjw.dev.types.framework.graph.processor.INodeProcessor;
import cn.hjw.dev.types.framework.graph.signal.Signal;
import cn.hjw.dev.types.framework.graph.signal.StandardSignal;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.function.BiFunction;


public class StandardGraphExecutor<T,C,R> implements IGraphExecutor<T,C,R> {

    private final GraphConfig graphConfig; // 图配置

    private final Map<String,Map<Signal,List<String>>> routeTable; // 路由表，key为节点ID，value为信号到节点处理器的映射表

    private final Map<String,INodeProcessor<T,C>> nodeProcessorTable; // 节点处理器表，key为节点ID，value为节点处理器实例

    private final BiFunction<T,C,R> terminalStrategy; // 获取图执行结果的策略函数

    public StandardGraphExecutor(GraphConfig graphConfig) {
        this.graphConfig = graphConfig;
        this.routeTable = graphConfig.getRouteTable();
        this.nodeProcessorTable = graphConfig.getNodeProcessorTable();
        this.terminalStrategy = graphConfig.getTerminalStrategy();

    }

    @Override
    public R execute(T request, C context) throws Exception {
        // 1. 使用队列替代单一指针 (广度优先遍历 BFS)
        Queue<String> pendingNodes = new ArrayDeque<>();
        if (ObjectUtils.isNotEmpty(graphConfig.getEntryPoint())) {
            pendingNodes.offer(graphConfig.getEntryPoint()); // 从入口节点开始执行
        }
        int stepCount = 0; // 步骤计数器，防止死循环
        final int MAX_STEPS = 1000; // 最大步骤数
        String currentNode;
        Signal pendingSignal;
        while (!pendingNodes.isEmpty() && stepCount < MAX_STEPS) {
            stepCount++;
            currentNode = pendingNodes.poll();
            // 获取当前节点处理器
            INodeProcessor<T, C> currentProcessor = nodeProcessorTable.getOrDefault(currentNode, null);
            if (ObjectUtils.isEmpty(currentProcessor)) {
                throw new IllegalStateException("Cannot find node processor for nodeId: " + currentNode);
            }
            // 执行当前节点处理器,获取处理结果信号
            pendingSignal = currentProcessor.process(request, context);
            if(StandardSignal.STOP.equals(pendingSignal)) {
                // 如果是 STOP，仅代表【当前这条分支】结束了
                // 不再往队列里添加后续节点，直接 continue 处理队列里的其他任务
                continue;
            }
            // 2.4 查路由表，找出所有后续节点
            Map<Signal, List<String>> signalToNodes = routeTable.getOrDefault(currentNode, Collections.emptyMap());
            List<String> nextNodes = signalToNodes.getOrDefault(pendingSignal, Collections.emptyList());
            // 2.5 将所有后续节点加入队列
            for (String nextNode : nextNodes) {
                pendingNodes.offer(nextNode);
            }
        }
        return terminalStrategy.apply(request, context);
    }


}
