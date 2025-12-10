package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.iterator.impl;

import cn.hjw.dev.types.design.ResponsibilityChain.model1.DoubleLink;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.handler.ChainHandler;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.iterator.AbstractChainIterator;

import java.util.function.BiFunction;


public class LinkedSequenceIterator<T,C,R> extends AbstractChainIterator<DoubleLink<ChainHandler<T, C, R>>, T, C, R> {


    private DoubleLink.Node<ChainHandler<T,C,R>> currentNode;

    private final DoubleLink.Node<ChainHandler<T,C,R>> lastNode;

    public LinkedSequenceIterator(DoubleLink<ChainHandler<T,C,R>> handlers, BiFunction<T,C,R> terminalStrategy) {
        super(handlers,terminalStrategy);
        this.lastNode = handlers.getLastNode();
        this.currentNode = handlers.getFirstNode();
    }

    @Override
    public boolean hasNext() {
        return this.currentNode != this.lastNode;
    }

    @Override
    protected void moveToNext() {
        this.currentNode = this.currentNode.next;
    }

    @Override
    protected ChainHandler<T, C, R> currentHandler() {
        return this.currentNode.val;
    }
}
