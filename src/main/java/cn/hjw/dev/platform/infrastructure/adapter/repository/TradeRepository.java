package cn.hjw.dev.platform.infrastructure.adapter.repository;

import cn.hjw.dev.platform.domain.trade.adapter.repository.ITradeRepository;
import cn.hjw.dev.platform.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import cn.hjw.dev.platform.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.hjw.dev.platform.domain.trade.model.entity.*;
import cn.hjw.dev.platform.domain.trade.model.valobj.ActivityStatusEnumVO;
import cn.hjw.dev.platform.domain.trade.model.valobj.GroupBuyOrderEnumVO;
import cn.hjw.dev.platform.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.hjw.dev.platform.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyActivityDao;
import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyOrderDao;
import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyOrderListDao;
import cn.hjw.dev.platform.infrastructure.dao.INotifyTaskDao;
import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyActivity;
import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyOrder;
import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyOrderList;
import cn.hjw.dev.platform.infrastructure.dao.po.NotifyTask;
import cn.hjw.dev.platform.infrastructure.dcc.DynamicConfigCenter;
import cn.hjw.dev.platform.types.common.Constants;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 交易仓储服务
 * @create 2025-01-11 09:17
 */
@Slf4j
@Repository
public class TradeRepository implements ITradeRepository {

    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private INotifyTaskDao notifyTaskDao;

    @Resource
    private DynamicConfigCenter dynamicConfigCenter; // 动态配置中心

    /**
     * 根据外部单号查询营销支付订单,如果存在说明已经锁单了
     * @param userId
     * @param outTradeNo
     * @return
     */
    @Override
    public MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setOutTradeNo(outTradeNo);
        GroupBuyOrderList groupBuyOrderListRes = groupBuyOrderListDao.queryGroupBuyOrderRecordByOutTradeNo(groupBuyOrderListReq);
        if (null == groupBuyOrderListRes) return null;

        return MarketPayOrderEntity.builder()
                .orderId(groupBuyOrderListRes.getOrderId())
                .deductionPrice(groupBuyOrderListRes.getDeductionPrice())
                .originalPrice(groupBuyOrderListRes.getOriginalPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.valueOf(groupBuyOrderListRes.getStatus()))
                .teamId(groupBuyOrderListRes.getTeamId())
                .build();
    }

    /**
     * 锁定，营销预支付订单；商品下单前，预购锁定。
     * @param groupBuyOrderAggregate
     * @return
     */
    @Transactional(timeout = 500,rollbackFor = Exception.class)
    @Override
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) throws NoSuchAlgorithmException {
        // 聚合根对象信息
        UserEntity userEntity = groupBuyOrderAggregate.getUserEntity();
        PayActivityEntity payActivityEntity = groupBuyOrderAggregate.getPayActivityEntity();
        PayDiscountEntity payDiscountEntity = groupBuyOrderAggregate.getPayDiscountEntity();
        Integer userTakeOrderCount = groupBuyOrderAggregate.getUserTakeOrderCount();

        // 判断是否有团 - teamId 为空 - 新团、为不空 - 老团
        String teamId = payActivityEntity.getTeamId();
        if (StringUtils.isBlank(teamId)) {
            // 使用 RandomStringUtils.randomNumeric 替代公司里使用的雪花算法UUID
            teamId = RandomStringUtils.randomNumeric(8);
            LocalDateTime startTime = LocalDateTime.now(); // 拼团有效开始时间
            LocalDateTime endTime = LocalDateTime.now().plusMinutes(payActivityEntity.getValidTime()); // 拼团有效结束时间
            // 构建拼团订单
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                    .teamId(teamId)
                    .activityId(payActivityEntity.getActivityId())
                    .source(payDiscountEntity.getSource())
                    .channel(payDiscountEntity.getChannel())
                    .originalPrice(payDiscountEntity.getOriginalPrice()) // 原始价格
                    .deductionPrice(payDiscountEntity.getDeductionPrice()) // 优惠价格
                    .payPrice(payDiscountEntity.getPayPrice()) // 当前支付价格
                    .targetCount(payActivityEntity.getTargetCount()) // 目标成团人数
                    .completeCount(0) // 完成成团人数
                    .lockCount(1) // 锁定成团人数
                    .notifyUrl(payDiscountEntity.getNotifyUrl()) // 回调通知地址
                    .validStartTime(startTime)
                    .validEndTime(endTime)
                    .build();
            // 写入记录
            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            // 不是新团，老团锁单，更新锁定数量+1
            // 更新记录 - 如果更新记录不等于1，则表示拼团已满，抛出异常
            int updateAddTargetCount = groupBuyOrderDao.updateAddLockCount(teamId);
            if (1 != updateAddTargetCount) {
                // 变更记录数不是1条
                throw new AppException(ResponseCode.E0005);
            }
        }
        // 构建用户拼团订单明细
        // 使用 RandomStringUtils.randomNumeric 替代公司里使用的雪花算法UUID
        String orderId = RandomStringUtils.randomNumeric(12); // 订单id是拼团系统的唯一标识
        String bizId = payActivityEntity.getActivityId() + Constants.UNDERLINE + userEntity.getUserId() + Constants.UNDERLINE +(userTakeOrderCount+1);
        String bizId64 = DigestUtils.sha256Hex(bizId.getBytes(StandardCharsets.UTF_8));

        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder()
                .userId(userEntity.getUserId())
                .teamId(teamId)
                .orderId(orderId)
                .activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime())
                .endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .bizId(bizId64) // 构建 bizId 唯一值；活动id_用户id_参与次数累加
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                .build();
        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderListReq);
        } catch (DuplicateKeyException e) { // 重复键异常，表示 outTradeNo 已存在
            log.error("lockMarketPayOrder#lock market pay order failed, " +
                    "duplicate outTradeNo. userId:{}, outTradeNo:{}", userEntity.getUserId(), payDiscountEntity.getOutTradeNo(), e);
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        return MarketPayOrderEntity.builder()
                .orderId(orderId)
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE)
                .teamId(teamId) // 返回团ID
                .payPrice(payDiscountEntity.getPayPrice())
                .build();
    }

    /**
     * 根据活动ID查询拼团活动实体
     * @param activityId
     * @return
     */
    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);
        return GroupBuyActivityEntity.builder()
                .activityId(groupBuyActivity.getActivityId())
                .activityName(groupBuyActivity.getActivityName())
                .discountId(groupBuyActivity.getDiscountId())
                .groupType(groupBuyActivity.getGroupType())
                .takeLimitCount(groupBuyActivity.getTakeLimitCount())
                .target(groupBuyActivity.getTarget())
                .validTime(groupBuyActivity.getValidTime())
                .status(ActivityStatusEnumVO.valueOf(groupBuyActivity.getStatus()))
                .startTime(groupBuyActivity.getStartTime())
                .endTime(groupBuyActivity.getEndTime())
                .tagId(groupBuyActivity.getTagId())
                .tagScope(groupBuyActivity.getTagScope())
                .build();
    }

    /**
     * 根据活动ID和用户ID查询用户拼团订单数量
     * @param activityId
     * @param userId
     * @return
     */
    @Override
    public Integer queryOrderCountByActivityId(Long activityId, String userId) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);
        return groupBuyOrderListDao.queryOrderCountByActivityId(groupBuyOrderListReq);
    }

    /**
    * 设置拼团结算，修改多个状态，要在一个事务中，同时成功，同时失败
    * @param groupBuyTeamSettlementAggregate
    * */
    @Transactional(timeout = 500,rollbackFor = Exception.class)
    @Override
    public boolean settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {

        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        TradePaySuccessEntity tradePaySuccessEntity = groupBuyTeamSettlementAggregate.getTradePaySuccessEntity();

        // 1. 更新拼团订单明细状态
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(userEntity.getUserId());
        groupBuyOrderListReq.setOutTradeNo(tradePaySuccessEntity.getOutTradeNo());
        groupBuyOrderListReq.setSource(tradePaySuccessEntity.getSource());
        groupBuyOrderListReq.setChannel(tradePaySuccessEntity.getChannel());
        groupBuyOrderListReq.setOutTradeTime(tradePaySuccessEntity.getOutTradeTime()); // 外部交易时间
        int updateOrderListStatusCount = groupBuyOrderListDao.updateOrderStatus2COMPLETE(groupBuyOrderListReq);
        if (1 != updateOrderListStatusCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        // 2. 更新拼团达成数量
        int updateAddCount = groupBuyOrderDao.updateAddCompleteCount(groupBuyTeamEntity.getTeamId());
        if (1 != updateAddCount) {
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        // 3. 更新拼团完成状态
        if (groupBuyTeamEntity.getTargetCount() - groupBuyTeamEntity.getCompleteCount() == 1) {
            // 当前是最后一人，更新拼团订单状态为完成
            int updateOrderStatusCount = groupBuyOrderDao.updateOrderStatus2COMPLETE(groupBuyTeamEntity.getTeamId()); // 更新拼团订单状态为完成
            if (1 != updateOrderStatusCount) {
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }

            // 拼团完成写入回调任务记录
            HashMap<String, Object> params = new HashMap<>();

            // 查询拼团交易完成外部单号列表(正常是发送订单号，现在改成用户ID列表)
            // List<String> outTradeNoList = groupBuyOrderListDao.queryGroupBuyCompleteOrderOutTradeNoListByTeamId(groupBuyTeamEntity.getTeamId());
            List<String> userIdList = groupBuyOrderListDao.queryGroupBuyCompleteOrderUserIdListByTeamId(groupBuyTeamEntity.getTeamId());
            params.put("teamId", groupBuyTeamEntity.getTeamId());
            params.put("userIdList", userIdList);
            userIdList.forEach(userId -> {
                List<String> goodsId = groupBuyOrderListDao.queryGoodsIdByUserIdAndTeamId(userId, groupBuyTeamEntity.getTeamId());
                params.put(userId, goodsId);
            }); // 用于扣减库存
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            notifyTask.setNotifyUrl(groupBuyTeamEntity.getNotifyUrl());
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setParameterJson(JSONObject.toJSONString(params));
            /*
            * {
            *  teamId: "12345678",
            *  userIdList: ["user1","user2","user3"],
            *  user1: [goodsId1, goodsId2],
            *  user2: [goodsId3, goodsId4],
            *  user3: [goodsId5, goodsId6]
            * }
            * */
            notifyTaskDao.insert(notifyTask);
            return true;
        }
        return false;
    }

    @Override
    public GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyTeamByTeamId(teamId);
        return GroupBuyTeamEntity.builder()
                .teamId(groupBuyOrder.getTeamId())
                .activityId(groupBuyOrder.getActivityId())
                .targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .notifyUrl(groupBuyOrder.getNotifyUrl())
                .status(GroupBuyOrderEnumVO.valueOf(groupBuyOrder.getStatus()))
                .validStartTime(groupBuyOrder.getValidStartTime()) // 拼团有效的开始时间
                .validEndTime(groupBuyOrder.getValidEndTime()) // 拼团有效的结束时间
                .build();
    }

    @Override
    public boolean isSCBlackIntercept(String source, String channel) {
        return dynamicConfigCenter.isSCBlackIntercept(source, channel);
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyProgress(teamId);
        if (null == groupBuyOrder) return null;
        return GroupBuyProgressVO.builder()
                .completeCount(groupBuyOrder.getCompleteCount())
                .targetCount(groupBuyOrder.getTargetCount())
                .lockCount(groupBuyOrder.getLockCount())
                .build();
    }

    /**
     * 更新通知任务状态为成功
     * @param teamId
     * @return
     */
    @Override
    public int updateNotifyTaskStatusSuccess(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusSuccess(teamId);
    }

    /**
     * 更新通知任务状态为重试
     * @param teamId
     * @return
     */
    @Override
    public int updateNotifyTaskStatusRetry(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusRetry(teamId);
    }

    /**
     * 更新通知任务状态为错误
     * @param teamId
     * @return
     */
    @Override
    public int updateNotifyTaskStatusError(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusError(teamId);
    }


    /**
     * 查询未执行的通知任务列表
     * @param teamId
     * @return
     */
    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId) {
        NotifyTask notifyTask = notifyTaskDao.queryUnExecutedNotifyTaskByTeamId(teamId);
        if (ObjectUtils.isEmpty(notifyTask)) return new ArrayList<>();
        return Collections.singletonList(NotifyTaskEntity.builder()
                .teamId(notifyTask.getTeamId())
                .notifyUrl(notifyTask.getNotifyUrl())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .build());
    }

    /**
     * 查询未执行的通知任务列表
     * @return
     */
    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList() {
        List<NotifyTask> notifyTaskList = notifyTaskDao.queryUnExecutedNotifyTaskList();
        // 限制50条
        if (notifyTaskList.isEmpty()) return Collections.emptyList();

        return notifyTaskList.stream().map(notifyTask -> NotifyTaskEntity.builder()
                .teamId(notifyTask.getTeamId())
                .notifyUrl(notifyTask.getNotifyUrl())
                .notifyCount(notifyTask.getNotifyCount())
                .parameterJson(notifyTask.getParameterJson())
                .build()).collect(Collectors.toList());
    }

}
