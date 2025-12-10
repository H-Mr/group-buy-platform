package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.chain;

public interface ExecutableChain<T,C,R> {

    /**
     * 执行责任链
     * @param request 请求参数
     * @param context 动态上下文
     * @return 返回责任链处理结果
     */
    R apply(T request, C context) throws Exception;


}
