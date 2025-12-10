package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.iterator;

/**
 * @author hjw
 * @description 责任链执行器接口，定义了链条中如何移动到下个节点
 * @create 2024-06-28 10:05
 */
public interface ChainIterator<T,C,R> {

    /**
     * 移动到下一个节点并执行
     * @param request
     * @param context
     * @return 返回责任链处理结果
     */
    R next(T request,C context) throws Exception;

    boolean hasNext();
}
