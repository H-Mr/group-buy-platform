package cn.hjw.dev.mall.types.design.tree;

/**
 * 在规则树（决策树）中，一个非叶子节点（中间节点）通常承载着双重责任：
 * 不仅要“做事”：它可能需要执行一些前置逻辑，比如参数校验、黑名单过滤、日志记录等（这是 StrategyHandler.apply 的职责）。
 * 还要“指路”：做完事后，它必须根据结果决定流程流向哪一个子节点（这是 StrategyMapper.get 的职责）。
 *AbstractStrategyRouter 通过同时实现这两个接口，强制要求所有继承它的子类（如 SwitchRoot, RootNode）必须同时具备“执行能力”和“路由能力”。
 *这样，每一个业务节点（Node）在架构上就变成了一个自包含的、完备的决策单元。
 * @param <T>
 * @param <C>
 * @param <R>

 */
public abstract class AbstractStrategyRouter<T,C,R> implements StrategyHandler<T,C,R>, StrategyMapper<T,C,R> {

    protected StrategyHandler<T,C,R> defaultStrategyHandler = StrategyHandler.DEFAULT;


    public R router(T request, C context) throws Exception {

        // 使用自身的路由功能获取下一个节点
        StrategyHandler<T,C,R> strategyHandler = this.next(request, context); // 使用next方法获取下一个节点
        if(null != strategyHandler) {
            // 执行下一个节点的功能
            return strategyHandler.handle(request, context);
        }
        return this.defaultStrategyHandler.handle(request, context);
    }

    /**
     * 把每个结点的处理方法模板化2个流程，先是异步加载数据，之后再执行逻辑
     * @param request
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    public R handle(T request, C context) throws Exception {
        // 异步多线程加载数据
        this.asyncLoadData(request,context);
        // 业务流程受理
        return this._handle(request,context);
    }


    /**
     * 异步加载数据
     * @param request
     * @param context
     */
    protected abstract void asyncLoadData(T request,C context) throws Exception;

    /**
     * 每个结点的处理逻辑
     * @param request
     * @param context
     * @return
     * @throws Exception
     */
    protected abstract R _handle(T request,C context) throws Exception;


}
