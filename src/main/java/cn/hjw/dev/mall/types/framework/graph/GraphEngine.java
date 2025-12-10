package cn.hjw.dev.mall.types.framework.graph;

import cn.hjw.dev.types.framework.graph.config.GraphConfig;
import cn.hjw.dev.types.framework.graph.executor.ExecutorFactory;
import cn.hjw.dev.types.framework.graph.executor.StandardGraphExecutor;

public class GraphEngine<T,C,R> implements ExecutableGraph<T,C,R> {

    private final ExecutorFactory<T,C,R> executorFactory = StandardGraphExecutor::new;

    private final GraphConfig<T,C,R> graphConfig;

    public GraphEngine(GraphConfig<T,C,R> graphConfig) {
        this.graphConfig = graphConfig;
    }

    @Override
    public R apply(T request, C context) throws Exception {
        return executorFactory.create(this.graphConfig).execute(request, context);
    }
}
