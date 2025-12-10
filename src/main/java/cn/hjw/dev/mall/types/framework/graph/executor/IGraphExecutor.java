package cn.hjw.dev.mall.types.framework.graph.executor;

/**
 * 图执行器接口
 * @author hjw
 * @date 2023/11/10
 */
public interface IGraphExecutor<T,C,R> {

    /**
     * 执行图节点
     * @param request 入参
     * @param context 上下文
     * @return 结果
     * @throws Exception 异常
     */
    R execute(T request, C context) throws Exception;

}
