package cn.hjw.dev.mall.types.framework.graph.config;

import cn.hjw.dev.types.framework.graph.processor.INodeProcessor;
import cn.hjw.dev.types.framework.graph.signal.Signal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class GraphConfig<T,C,R> {

    private Map<String,Map<Signal, List<String>>> routeTable; // 路由表，key为节点ID，value为信号到节点处理器的映射表

    private Map<String,INodeProcessor<T,C>> nodeProcessorTable; // 节点处理器表，key为节点ID，value为节点处理器实例

    private BiFunction<T,C,R> terminalStrategy; // 获取图执行结果的策略函数

    private String entryPoint; // 入口节点

    /**
     * 添加路由规则 (支持多次调用添加多个目标)
     */
    public GraphConfig<T,C,R> addRoute(String currentId, Signal signal, String nextId) {
        if (ObjectUtils.isEmpty(routeTable)) {
            routeTable = new HashMap<>();
        }
        routeTable.computeIfAbsent(currentId, k -> new HashMap<>())
                .computeIfAbsent(signal, k -> new ArrayList<>())
                .add(nextId);
        return this;
    }

}
