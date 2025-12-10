package cn.hjw.dev.mall.types.design.ResponsibilityChain.model2.chain;

import cn.hjw.dev.types.design.ResponsibilityChain.model2.factory.IteratorFactory;
import cn.hjw.dev.types.design.ResponsibilityChain.model2.handler.ChainHandler;
import lombok.Setter;

import java.util.function.BiFunction;

public abstract class AbstractChain<S,T,C,R> implements ExecutableChain<T,C,R> {

    // 1. 持有执行器工厂，且工厂的数据源类型必须是 S,子类可以访问这个变量
    @Setter
    protected IteratorFactory<S, T, C, R> iteratorFactory;

    // 2. 兜底策略
    @Setter
    protected BiFunction<T,C,R> terminalStrategy;

    protected final String name;

    public AbstractChain(String name) {
        this.name = name;
        this.setTerminalStrategy((r,c) -> null); // 默认终止策略，返回 null
    }

    /**
     * 获取链的存储容器
     * @return
     */
    protected abstract S getSource();

    public R apply(T request, C context) throws Exception {
        return this.iteratorFactory.create(getSource(),terminalStrategy).next(request, context);
    }

    // 注册处理器
    public abstract void registerHandler(ChainHandler<T, C, R>... handlers);
}
