package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.chain.impl;

import cn.hjw.dev.types.design.ResponsibilityChain.model2.chain.AbstractChain;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.handler.ChainHandler;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.iterator.impl.ArraySequenceIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayChain<T,C,R> extends AbstractChain<List<ChainHandler<T,C,R>>,T,C,R> {

    private final List<ChainHandler<T,C,R>> handlers;

    public ArrayChain(String name) {
        super(name);
        this.handlers = new ArrayList<>();
        this.setIteratorFactory(ArraySequenceIterator<T,C,R>::new);
    }

    @Override
    protected List<ChainHandler<T,C,R>> getSource() {
        return this.handlers;
    }

    @SafeVarargs
    @Override
    public final void registerHandler(ChainHandler<T, C, R>... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
    }

}
