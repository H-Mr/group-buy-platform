package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.chain.impl;

import cn.hjw.dev.types.design.ResponsibilityChain.model1.DoubleLink;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.chain.AbstractChain;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.handler.ChainHandler;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.iterator.impl.LinkedSequenceIterator;


/**
 * @author hjw
 * @description 定义责任链管道，只负责链条的组装和管理
 * @create 2024-06-28 10:01
 */
public class LinkedChain<T,C,R> extends AbstractChain<DoubleLink<ChainHandler<T,C,R>>,T,C,R> {

    private final DoubleLink<ChainHandler<T,C,R>> handlers;

    public LinkedChain(String name) {
        super(name);
        handlers = new DoubleLink<>(name);
        this.setIteratorFactory(LinkedSequenceIterator<T,C,R>::new);
    }

    @Override
    protected DoubleLink<ChainHandler<T, C, R>> getSource() {
        return this.handlers;
    }

    /**
     * 注册处理器
     * @param handlers
     */
    @SafeVarargs
    public final void registerHandler(ChainHandler<T, C, R>... handlers) {
        for (ChainHandler<T,C,R> handler : handlers) {
            this.handlers.add(handler);
        }
    }

}
