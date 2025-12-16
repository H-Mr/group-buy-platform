package cn.hjw.dev.platform.infrastructure.dao;

import cn.hjw.dev.platform.infrastructure.dao.po.NotifyTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 回调任务
 * @create 2025-01-26 18:23
 */
@Mapper
public interface INotifyTaskDao {

    void insert(NotifyTask notifyTask);

    List<NotifyTask> queryUnExecutedNotifyTaskList();

    NotifyTask queryUnExecutedNotifyTaskByTeamId(String teamId);

    int updateNotifyTaskStatusSuccess(String teamId);

    int updateNotifyTaskStatusError(String teamId);

    int updateNotifyTaskStatusRetry(String teamId);

}
