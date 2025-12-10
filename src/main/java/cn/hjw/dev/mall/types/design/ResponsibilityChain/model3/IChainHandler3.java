package cn.hjw.dev.mall.types.design.ResponsibilityChain.model3;

/**
 * @author hjw
 * @description 定义了责任链中每个节点的处理逻辑
 * @create 2024-06-28 10:01
 */
public interface IChainHandler3<T,C,R> extends IChainAssembler<T,C,R> {

    R handle(T requestParameter, C dynamicContext) throws Exception;
}
