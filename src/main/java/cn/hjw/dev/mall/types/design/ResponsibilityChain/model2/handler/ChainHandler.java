package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.handler;

import cn.hjw.dev.types.design.ResponsibilityChain.model2.iterator.ChainIterator;

public interface ChainHandler<T,C,R> {
    /**
     * 责任链每个节点的处理器
     * @param request
     * @param context
     * @return
     * @throws Exception
     */
    R handle(T request, C context, ChainIterator<T,C,R> executor) throws Exception;
}
