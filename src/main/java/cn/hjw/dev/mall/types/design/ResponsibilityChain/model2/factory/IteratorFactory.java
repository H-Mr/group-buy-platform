package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.factory;

import cn.hjw.dev.types.design.ResponsibilityChain.model2.iterator.ChainIterator;

import java.util.function.BiFunction;

/**
 * @author hjw
 * @description 责任链迭代器工厂接口
 * @param <S> 底层存储结构 (List, DoubleLink, Array, Map...)
 */
@FunctionalInterface
public interface IteratorFactory<S, T, C, R> {
    // 使用泛型 S (Source) 解耦底层存储结构。 工厂接口不应该规定底层是 List，而应该由 Container 告诉工厂“我底层存的是什么”。
    /**
     * @param source 数据源 (Container 传进来的底层结构)
     * @param terminalStrategy 兜底策略 (Container 传进来的兜底逻辑)
     */
    ChainIterator<T, C, R> create(S source, BiFunction<T,C,R> terminalStrategy);
}
