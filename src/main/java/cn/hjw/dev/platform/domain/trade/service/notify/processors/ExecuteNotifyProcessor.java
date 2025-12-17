package cn.hjw.dev.platform.domain.trade.service.notify.processors;

import cn.hjw.dev.platform.domain.trade.adapter.port.ITradePort;
import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.entity.NotifyRequestEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.NotifyTaskEntity;
import cn.hjw.dev.platform.domain.trade.service.notify.factory.NotifyDAGFactory;
import cn.hjw.dev.platform.types.enums.NotifyTaskHTTPEnumVO;
import cn.hjw.dev.dagflow.processor.DAGNodeProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ExecuteNotifyProcessor implements DAGNodeProcessor<NotifyRequestEntity, NotifyDAGFactory.NotifyContext, NotifyDAGFactory.NotifyNodeResult> {

    @Resource
    private ITradePort port;
    @Resource
    private ITradeRepository tradeRepository;

    @Override
    public NotifyDAGFactory.NotifyNodeResult process(NotifyRequestEntity req, NotifyDAGFactory.NotifyContext ctx) throws Exception {
        List<NotifyTaskEntity> taskList = ctx.getTaskList();
        Map<String, Integer> resultMap = new HashMap<>();
        if (taskList == null || taskList.isEmpty()) {
            log.info("DAG-Notify: 无需执行的任务");
            return new NotifyDAGFactory.NotifyNodeResult(NotifyDAGFactory.NotifyNodeResult.TYPE_EXEC, resultMap);
        }
        int successCount = 0, errorCount = 0, retryCount = 0;

        for (NotifyTaskEntity notifyTask : taskList) {
              try {
                  // 回调处理 success 成功，error 失败
                  String response = port.groupBuyNotify(notifyTask);

                  // 更新状态判断&变更数据库表回调任务状态
                  if (NotifyTaskHTTPEnumVO.SUCCESS.getCode().equals(response)) {
                      int updateCount = tradeRepository.updateNotifyTaskStatusSuccess(notifyTask.getTeamId());
                      if (1 == updateCount) {
                          successCount += 1;
                      }
                  } else if (NotifyTaskHTTPEnumVO.ERROR.getCode().equals(response)) {
                      if (notifyTask.getNotifyCount() < 5) { // 小于 5 次重试
                          int updateCount = tradeRepository.updateNotifyTaskStatusRetry(notifyTask.getTeamId());
                          if (1 == updateCount) {
                              retryCount += 1;
                          }
                      } else { // 超过 5 次则标记为失败
                          int updateCount = tradeRepository.updateNotifyTaskStatusError(notifyTask.getTeamId());
                          if (1 == updateCount) {
                              errorCount += 1;
                          }
                      }
                  }
              } catch (Exception e) {
                  log.error("DAG-Notify: 执行回调任务异常，teamId: {}, error: {}", notifyTask.getTeamId(), e.getMessage(), e);
              }
        }

        log.info("DAG-Notify: 批量执行完成，总数:{} 成功:{} 失败: {} 重试： {}", taskList.size(), successCount,errorCount,retryCount);
        resultMap.put("waitCount", taskList.size());
        resultMap.put("successCount", successCount);
        resultMap.put("errorCount", errorCount);
        resultMap.put("retryCount", retryCount);
        return new NotifyDAGFactory.NotifyNodeResult(NotifyDAGFactory.NotifyNodeResult.TYPE_EXEC, resultMap);
    }

}
