package cn.hjw.dev.mall.types.design.ResponsibilityChain.model1;

import cn.hjw.dev.types.design.ResponsibilityChain.model1.handler.IChainHandler1;

public class ChainProcessor<T,C,R> extends DoubleLink<IChainHandler1<T,C,R>> {

    public ChainProcessor(String name) {
        super(name);
    }

    /**
     * 注册处理器
     * @param handlers
     */
    @SafeVarargs
    public final void registerHandler(IChainHandler1<T,C,R>... handlers) {
        for (IChainHandler1<T,C,R> handler : handlers) {
            this.add(handler);
        }
    }

    public R apply(T request, C context) throws Exception {
        Node<IChainHandler1<T,C,R>> current = this.first.next;
        while(current != this.last) {
            IChainHandler1<T,C,R> handler = current.val;
            R result = handler.handle(request, context);
            if (result != null) {
                return result;
            }
            current = current.next;
        }
        return null;
    }
}
