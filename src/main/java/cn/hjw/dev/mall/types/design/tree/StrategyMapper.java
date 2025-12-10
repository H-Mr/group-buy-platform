package cn.hjw.dev.mall.types.design.tree;

/**
 * @description 策略映射器
 * T 入参类型
 * D 上下文参数
 * R 返参类型
 */
@FunctionalInterface
public interface StrategyMapper<T,C,R> {

    /**
     * 根据入参和上下文，获取对应的策略处理器
     * @param request 入参
     * @param context 上下文
     * @return 策略处理器
     * @throws Exception
     */
    StrategyHandler<T,C,R> next(T request, C context) throws Exception;


}
