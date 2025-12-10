package cn.hjw.dev.mall.types.framework.graph.signal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标准通用信号枚举
 */
@Getter
@AllArgsConstructor
public enum StandardSignal implements Signal {

    /**
     * 继续：代表当前节点执行成功，请引擎继续执行配置中的下一个节点
     * (对应责任链的 next)
     */
    NEXT("NEXT", "继续执行"),

    /**
     * 停止：代表流程在此处正常终止，不再往后执行
     * (对应责任链的拦截/返回)
     */
    STOP("STOP", "流程终止"),

    /**
     * 异常：代表节点执行出错，引擎应根据配置决定是重试、回滚还是报错
     */
    FAIL("FAIL", "执行失败");

    private final String code;
    private final String info;
}
