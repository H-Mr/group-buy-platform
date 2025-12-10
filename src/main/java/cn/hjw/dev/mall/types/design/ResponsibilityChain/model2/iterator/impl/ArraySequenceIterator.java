package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.iterator.impl;

import cn.hjw.dev.types.design.ResponsibilityChain.model2.handler.ChainHandler;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.iterator.AbstractChainIterator;

import java.util.List;
import java.util.function.BiFunction;

public class ArraySequenceIterator<T,C,R> extends AbstractChainIterator<List<ChainHandler<T, C, R>>, T, C, R> {

    // ArraySequenceIterator的具体实现依赖
    private int currentIndex;
    private final int length;

    public ArraySequenceIterator(List<ChainHandler<T,C,R>> handlers, BiFunction<T,C,R> terminateStrategy) {
        super(handlers, terminateStrategy);
        this.currentIndex = 0;
        this.length = handlers.size();
    }

    @Override
    public boolean hasNext() {
        return this.currentIndex < this.length;
    }

    @Override
    protected void moveToNext() {
        this.currentIndex++;
    }

    @Override
    protected ChainHandler<T, C, R> currentHandler() {
        return this.handlerSource.get(this.currentIndex);
    }
}
