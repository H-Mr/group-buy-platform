package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.handler;

import cn.hjw.dev.types.design.ResponsibilityChain.model2.iterator.ChainIterator;

public abstract class AbstractChainHandler<T,C,R> implements ChainHandler<T,C,R> {

    @Override
    public R handle(T request, C context, ChainIterator<T, C, R> iterator) throws Exception {
            // 1. 前置过滤：如果不满足条件，直接放行给下一个，不执行当前逻辑
            if (!shouldHandle(request, context)) {
                return iterator.next(request, context);
            }

            // 2. 执行当前节点的具体业务逻辑
            // 注意：这里不需要手动调 next，也不需要返回最终结果，只关注副作用（修改 Context）
            if (doHandle(request, context)) {
                // 3. 自动交还控制权，执行下一个节点，这样子类就完全不用感知 executor 的存在了
                return iterator.next(request, context);
            } else {
                return this.produceResult(request, context);
            }

    }
    protected boolean doHandle(T request, C context) throws Exception {

        this.asyncLoadData(request, context); // 异步加载数据
        this.process(request, context); // 具体节点处理逻辑
        return true;
    }

    /**
     * 异步加载数据
     * @param request
     * @param context
     */
    protected abstract void asyncLoadData(T request,C context) throws Exception;


    /**
     * 具体节点处理逻辑，由子类实现
     * @param request
     * @param context
     * @throws Exception
     */
    protected abstract void process(T request, C context) throws Exception;


    /**
     * 可选钩子：是否需要处理该请求，默认总是处理
     */
    protected boolean shouldHandle(T request, C context) {
        return true;
    }

    /**
     * 【中断时的返回值】
     * 默认返回 null，子类可以重写此方法返回具体结果（或从 Context 中拿）
     */
    protected R produceResult(T request, C context) {
        return null;
    }
}
