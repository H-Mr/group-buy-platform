package cn.hjw.dev.platform.trigger.listener;

import cn.hjw.dev.platform.domain.trade.event.GroupBuyCompletedEventTypeType;
import cn.hjw.dev.platform.domain.trade.model.entity.NotifyRequestEntity;
import cn.hjw.dev.platform.domain.trade.service.notify.factory.NotifyDAGFactory;
import cn.hjw.dev.platform.types.event.BaseEventType;
import cn.hjw.dev.dagflow.ExecutableGraph;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
public class NotifyEventListener {

    @Resource(name = "sys-event-bus")
    private EventBus eventBus; // 事件总线

    @Resource(name = "notifyDAGEngine")
    private ExecutableGraph<NotifyRequestEntity, NotifyDAGFactory.NotifyContext, Map<String,Integer>> notifyDAGEngine;

    @PostConstruct
    public void init() {
        // 将当前类注册到 Guava EventBus
        eventBus.register(this);
    }

    /**
     * 订阅通知事件
     * Guava EventBus 是同步的，如果需要异步，可以在 DAG 内部使用线程池，或者配置 AsyncEventBus
     * 但由于我们的 DAG Engine 本身就是基于线程池执行的，所以这里直接调用即可实现异步效果。
     */
    @Subscribe
    public void handleNotifyEvent(BaseEventType.Message<GroupBuyCompletedEventTypeType.GroupBuyCompleted> event) {
        Object payload = event.getPayload();
        if (!(payload instanceof GroupBuyCompletedEventTypeType.GroupBuyCompleted)) {
            return;
        }
        GroupBuyCompletedEventTypeType.GroupBuyCompleted completed = (GroupBuyCompletedEventTypeType.GroupBuyCompleted) payload;

        String teamId = completed.getTeamId();
        log.info("EventBus 收到通知事件: teamId={}", teamId == null ? "NULL(全量扫描)" : teamId);

        try {
            // 1. 构建请求 (teamId 为空则代表扫描模式)
            NotifyRequestEntity request = new NotifyRequestEntity(teamId);

            // 2. 启动 DAG
            // DAG 内部自包含逻辑：有 teamId 查单个，无 teamId 查一批
            Map<String, Integer> execRes = notifyDAGEngine.apply(request, new NotifyDAGFactory.NotifyContext());
            log.info("通知DAG执行完成，结果统计: {}", execRes);

        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

}
