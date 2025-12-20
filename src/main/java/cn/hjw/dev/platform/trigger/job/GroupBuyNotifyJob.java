package cn.hjw.dev.platform.trigger.job;

import cn.hjw.dev.platform.domain.trade.event.GroupBuyCompletedEventTypeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 拼团完结回调通知任务；拼团回调任务表，实际公司场景会定时清理数据结转，不会有太多数据挤压
 * @create 2025-01-31 10:27
 */
@Slf4j
@Service
public class GroupBuyNotifyJob {


    @Resource
    private GroupBuyCompletedEventTypeType groupBuyCompletedEventType; // 注入组队完成事件

    @Scheduled(cron = "0 * * * * ?")
    public void exec() {
        try {

            log.info("定时任务触发：发送全量扫描通知事件");
            groupBuyCompletedEventType.publishGroupBuyCompleted(null);
        } catch (Exception e) {
            log.error("定时任务，回调通知拼团完结任务失败", e);
        }
    }

}
