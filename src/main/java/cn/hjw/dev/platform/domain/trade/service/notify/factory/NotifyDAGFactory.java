package cn.hjw.dev.platform.domain.trade.service.notify.factory;

import cn.hjw.dev.platform.domain.trade.model.entity.NotifyRequestEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.NotifyTaskEntity;
import cn.hjw.dev.platform.domain.trade.service.notify.processors.ExecuteNotifyProcessor;
import cn.hjw.dev.platform.domain.trade.service.notify.processors.LoadNotifyTaskProcessor;
import cn.hjw.dev.dagflow.ExecutableGraph;
import cn.hjw.dev.dagflow.config.GraphConfig;
import cn.hjw.dev.dagflow.engine.DAGEngine;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Configuration
public class NotifyDAGFactory {

    // 注入 DAG 专用的大池子
    @Resource(name = "dagExecutor")
    private ExecutorService dagExecutor;

    @Bean("notifyDAGEngine")
    public ExecutableGraph<NotifyRequestEntity, NotifyContext, Map<String,Integer>> notifyDAGEngine(
            LoadNotifyTaskProcessor loadProcessor,
            ExecuteNotifyProcessor executeProcessor
    ) {
        GraphConfig<NotifyRequestEntity,
                NotifyContext,
                NotifyNodeResult,
                Map<String,Integer>> config = new GraphConfig<>(dagExecutor);

        // 1. 注册节点
        config.addNode("loadTask", loadProcessor)
                .addNode("executeTask", executeProcessor);

        // 2. 编排：先加载，再执行
        config.addRoute("loadTask", "executeTask");

        // 3. 更新策略
        config.setUpdateStrategy((req, ctx, res) -> {
            if (ObjectUtils.isEmpty(res)) {
                return;
            }
            if(res.getType().equals(NotifyNodeResult.TYPE_LOAD)) {
                // 加载任务结果，放入上下文
                ctx.setTaskList((List<NotifyTaskEntity>) res.getData());
            } else {
                // 执行任务结果，统计结果放入上下文
                ctx.setExecResultMap((Map<String, Integer>) res.getData());
            }
        });

        // 4. 终止条件：所有节点执行完毕
        config.setTerminalStrategy((req, ctx) -> ctx.getExecResultMap());

        return new DAGEngine<>(config);
    }

    // 上下文：持有待执行的任务列表
    @Data
    public static class NotifyContext {
        private List<NotifyTaskEntity> taskList;
        private Map<String, Integer> execResultMap;
    }

    // 节点结果
    @Data
    @AllArgsConstructor
    public static class NotifyNodeResult {
        private String type;
        private Object data;

        public static final String TYPE_LOAD = "LOAD";
        public static final String TYPE_EXEC = "EXEC";
    }
}
