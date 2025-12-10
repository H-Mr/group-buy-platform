package cn.hjw.dev.mall.types.framework.graph.signal;

/**
 * 流程信号接口
 * 作用：节点执行后的返回结果，用于驱动引擎流转
 */
public interface Signal {

    /**
     * 获取信号标识（用于路由匹配）
     * 例如：NEXT, STOP, VIP, MALE
     */
    String getCode();

    /**
     * 获取信号描述（用于日志记录）
     */
    String getInfo();
}
/**
 * 设计意图说明：
 *
 * 解耦：节点不需要知道 NEXT 意味着去 B 节点还是 C 节点，它只是告诉引擎“我完事了，请继续”。
 *
 * 扩展性：业务层可以定义自己的 enum TrialSignal implements Signal（如 STOCK_EMPTY, RISK_HIGH），引擎完全兼容。
 *
 */