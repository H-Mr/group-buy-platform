package cn.hjw.dev.mall.types.design.ResponsibilityChain.model3;

public abstract class AbstractChain3<T,C,R> implements IChainHandler3<T,C,R> {

    // 每个节点持有下一个节点的引用
    private IChainHandler3<T,C,R> next;


    // 暴露 “下一个节点的引用”
    @Override
    public IChainHandler3<T, C, R> next() {
        return this.next;
    }

    // 添加下一个节点，来组装链,可以链式装配
    @Override
    public IChainHandler3<T,C,R> appendNext(IChainHandler3<T, C, R> next) {
        this.next = next;
        return this.next;
    }


    // 工具方法，当前节点处理完自身逻辑后，需要将请求传递给下一个节点时，不需要手动写 next().apply(...)，而是直接调用 next(参数) 即可
    protected R next(T requestParameter, C dynamicContext) throws Exception {
        if (this.next == null) {
            // 处理链终止逻辑，如返回默认值或抛出特定异常
            return null;
        }
        return this.next.handle(requestParameter, dynamicContext);
    }
}
