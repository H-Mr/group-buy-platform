package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.iterator;

import cn.hjw.dev.types.design.ResponsibilityChain.model2.handler.ChainHandler;

import java.util.function.BiFunction;

/**
 * 模板化执行器
 * 将 next() 的流程固定，将"如何找下一个"解耦
 */
public abstract class AbstractChainIterator<S,T,C,R> implements ChainIterator<T,C,R> {

    protected final BiFunction<T,C,R> terminateStrategy; // 终止策略

    protected final S handlerSource; // 当前迭代器处理的处理器数据源

    public AbstractChainIterator(S handlerSource,BiFunction<T,C,R> terminateStrategy) {
        // 如果没有提供终止策略，默认返回 null
        this.terminateStrategy = terminateStrategy != null ? terminateStrategy : (r,c) -> null;
        this.handlerSource = handlerSource;
    }

    @Override
    public R next(T request, C context) throws Exception {
        if(!hasNext()) {
            return this.terminateStrategy.apply(request, context);
        }
        ChainHandler<T,C,R> handler = this.currentHandler(); // 获取当前节点的处理器
        moveToNext(); // 移动到下一个节点
        return handler.handle(request, context, this); // 执行当前节点的处理器，并传入当前执行器
    }

    /**
     * 是否还有下一个节点
     */
    public abstract boolean hasNext();

    /**
     * 移动到下一个节点
     */
    protected abstract void moveToNext();

    /**
     * 获取当前节点的处理器
     */
    protected abstract ChainHandler<T,C,R> currentHandler();
}
