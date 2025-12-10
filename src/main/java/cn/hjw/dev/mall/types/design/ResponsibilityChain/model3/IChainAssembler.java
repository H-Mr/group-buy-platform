package cn.hjw.dev.mall.types.design.ResponsibilityChain.model3;

/**
 * @author hjw
 * @description 定义了如何获取和链接责任链
 * @create 2024-06-28 10:00
 */
public interface IChainAssembler<T,C,R> {

    /**
     * 获取下一个处理节点
     * @return
     */
    IChainHandler3<T,C,R> next();

    /**
     * 添加下一个处理节点
     * @param next
     */
    IChainHandler3<T,C,R> appendNext(IChainHandler3<T,C,R> next);

}
