package cn.hjw.dev.mall.types.framework.graph.executor;

import cn.hjw.dev.types.framework.graph.config.GraphConfig;

@FunctionalInterface
public interface ExecutorFactory<T,C,R> {

    /**
     * 创建图执行器
     * @param graphConfig
     * @return
     */
    IGraphExecutor<T,C,R> create(GraphConfig<T,C,R> graphConfig);
}
