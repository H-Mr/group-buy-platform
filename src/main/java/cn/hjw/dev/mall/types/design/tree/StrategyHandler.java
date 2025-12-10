package cn.hjw.dev.mall.types.design.tree;

/**
 * 标准化行为：它规定了规则树中每一个节点（无论是根节点、逻辑判断节点，还是最终计算节点）
 * 必须具备的能力——即“接收参数，执行逻辑，返回结果”。
 * @param <T>
 * @param <C>
 * @param <R>
 */
@FunctionalInterface
public interface StrategyHandler<T,C,R> {

    // 默认兜底策略，不做任何处理直接返回 null
    StrategyHandler DEFAULT = (request, context) -> null;

    /**
     *
     * @param request
     * @param context
     * @return
     * @throws Exception
     */
    R handle(T request, C context) throws Exception;
}
