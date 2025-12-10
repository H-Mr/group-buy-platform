package cn.hjw.dev.mall.types.framework.chain.iterate;

import cn.hjw.dev.types.framework.graph.processor.INodeProcessor;
import cn.hjw.dev.types.framework.graph.signal.Signal;
import cn.hjw.dev.types.framework.graph.signal.StandardSignal;

public abstract class AbstractChainProcessor<T,C> implements INodeProcessor<T,C> {
    @Override
    public Signal process(T request, C context) throws Exception {
        this.doProcess(request, context);
        return StandardSignal.NEXT;
    }

    /**
     * 具体处理逻辑，由子类实现
     * @param request
     * @param context
     * @throws Exception
     */
    protected abstract void doProcess(T request, C context) throws Exception;
}
