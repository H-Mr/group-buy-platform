package cn.hjw.dev.platform.domain.trade.adapter.repository;

import cn.hjw.dev.platform.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import cn.hjw.dev.platform.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.MarketPayOrderEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.NotifyTaskEntity;
import cn.hjw.dev.platform.domain.trade.model.valobj.GroupBuyProgressVO;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface ITradeRepository {

    MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo);

    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) throws NoSuchAlgorithmException;

    GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    boolean settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    boolean isSCBlackIntercept(String source, String channel);

    int updateNotifyTaskStatusSuccess(String teamId);

    int updateNotifyTaskStatusRetry(String teamId);

    int updateNotifyTaskStatusError(String teamId);

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList();

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId);
}
