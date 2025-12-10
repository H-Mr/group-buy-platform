package cn.hjw.dev.mall.types.design.ResponsibilityChain.model1.handler;

public interface IChainHandler1<T,C,R> {
    /**
     * 责任链每个节点的处理器
     * @param request
     * @param context
     * @return
     * @throws Exception
     */
    R handle(T request, C context) throws Exception;
}
