package cn.hjw.dev.platform.domain.trade.service.notify.processors;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.NotifyRequestEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.NotifyTaskEntity;
import cn.hjw.dev.platform.domain.trade.service.notify.factory.NotifyDAGFactory;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class LoadNotifyTaskProcessor implements DAGNodeProcessor<NotifyRequestEntity, NotifyDAGFactory.NotifyContext, NotifyDAGFactory.NotifyNodeResult> {

    @Resource
    private ITradeRepository repository;

    @Override
    public NotifyDAGFactory.NotifyNodeResult process(NotifyRequestEntity req, NotifyDAGFactory.NotifyContext ctx) {
        List<NotifyTaskEntity> tasks;

        try {
            if (StringUtils.isNotBlank(req.getTeamId())) {
                // 【场景A】：实时回调 - 针对单个 teamId
                log.info("DAG-Notify: 加载单个任务 teamId:{}", req.getTeamId());
                tasks = repository.queryUnExecutedNotifyTaskList(req.getTeamId());
            } else {
                // 【场景B】：定时补偿 - 扫描未完成任务 (limit 50)
                log.info("DAG-Notify: 扫描批量补偿任务");
                tasks = repository.queryUnExecutedNotifyTaskList();
            }
        } catch (Exception e) {
            log.error("DAG-Notify: 加载回调任务失败", e);
            throw new AppException(ResponseCode.UN_ERROR, "加载回调任务失败");
        }

        if (tasks.isEmpty())
            throw new AppException(ResponseCode.UN_ERROR,"没有查询到回调任务");

        return new NotifyDAGFactory.NotifyNodeResult(NotifyDAGFactory.NotifyNodeResult.TYPE_LOAD, tasks);
    }
}
