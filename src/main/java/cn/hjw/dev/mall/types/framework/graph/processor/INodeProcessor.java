package cn.hjw.dev.mall.types.framework.graph.processor;

import cn.hjw.dev.types.framework.graph.signal.Signal;

public interface INodeProcessor<T,C> {
    /**
     * 责任链每个节点的处理器
     * @param request
     * @param context
     * @return
     * @throws Exception
     */
    Signal process(T request, C context) throws Exception;
}
