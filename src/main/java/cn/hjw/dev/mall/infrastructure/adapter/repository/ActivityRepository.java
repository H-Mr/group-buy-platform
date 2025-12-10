package cn.hjw.dev.mall.infrastructure.adapter.repository;

import cn.hjw.dev.domain.activity.adapter.repository.IActivityRepository;
import cn.hjw.dev.domain.activity.model.valobj.DiscountTypeEnum;
import cn.hjw.dev.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.domain.activity.model.valobj.SCSkuActivityVO;
import cn.hjw.dev.domain.activity.model.valobj.SkuVO;
import cn.hjw.dev.infrastructure.dao.IGroupBuyActivityDao;
import cn.hjw.dev.infrastructure.dao.IGroupBuyDiscountDao;
import cn.hjw.dev.infrastructure.dao.ISCSkuActivityDao;
import cn.hjw.dev.infrastructure.dao.ISkuDao;
import cn.hjw.dev.infrastructure.dao.po.GroupBuyActivity;
import cn.hjw.dev.infrastructure.dao.po.GroupBuyDiscount;
import cn.hjw.dev.infrastructure.dao.po.SCSkuActivity;
import cn.hjw.dev.infrastructure.dao.po.Sku;
import cn.hjw.dev.infrastructure.dcc.DynamicConfigCenter;
import cn.hjw.dev.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @description 活动仓储
 * @create 2024-12-21 10:10
 */
@Slf4j
@Repository
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;
    @Resource
    private ISkuDao skuDao;
    @Resource
    private ISCSkuActivityDao skuActivityDao;
    @Resource
    private IRedisService redisService;

    @Resource
    private DynamicConfigCenter dynamicConfigCenter;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId) {

        GroupBuyActivity groupBuyActivityRes = groupBuyActivityDao.queryValidGroupBuyActivityId(activityId);
        if (ObjectUtils.isEmpty(groupBuyActivityRes)) {
            return null;
        }
        String discountId = groupBuyActivityRes.getDiscountId();
        // 查询活动对应的折扣
        GroupBuyDiscount groupBuyDiscountRes = groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(discountId);
        if (ObjectUtils.isEmpty(groupBuyDiscountRes)) {
            return null;
        }
        // 组装GroupBuyDiscount对象
        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
                .discountName(groupBuyDiscountRes.getDiscountName()) // 折扣名称
                .discountDesc(groupBuyDiscountRes.getDiscountDesc()) // 折扣描述
                .discountType(DiscountTypeEnum.getType(groupBuyDiscountRes.getDiscountType())) // 折扣类型
                .marketPlan(groupBuyDiscountRes.getMarketPlan()) // 营销方案
                .marketExpr(groupBuyDiscountRes.getMarketExpr()) // 营销表达式
                .tagId(groupBuyDiscountRes.getTagId()) // 人群标签ID
                .build();

        // 组装拼团活动的值对象
        return GroupBuyActivityDiscountVO.builder()
                .activityId(groupBuyActivityRes.getActivityId()) // 活动ID
                .activityName(groupBuyActivityRes.getActivityName()) // 活动名称
                .groupBuyDiscount(groupBuyDiscount) // 设置折扣对象
                .groupType(DiscountTypeEnum.getType(groupBuyActivityRes.getGroupType())) // 拼团类型
                .takeLimitCount(groupBuyActivityRes.getTakeLimitCount()) // 限购数量
                .target(groupBuyActivityRes.getTarget()) // 拼团目标
                .validTime(groupBuyActivityRes.getValidTime()) // 拼团时长
                .status(groupBuyActivityRes.getStatus()) // 活动状态
                .startTime(groupBuyActivityRes.getStartTime()) // 活动开始时间
                .endTime(groupBuyActivityRes.getEndTime()) // 活动结束时间
                .tagId(groupBuyActivityRes.getTagId()) // 人群标签ID
                .tagScope(groupBuyActivityRes.getTagScope()) // 人群标签范围
                .build();
    }

    @Override
    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        if (ObjectUtils.isEmpty(sku)) {
            return null;
        }
        return SkuVO.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .build();
    }

    @Override
    public SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId) {
        SCSkuActivity scSkuActivityReq = new SCSkuActivity();
        scSkuActivityReq.setSource(source);
        scSkuActivityReq.setChannel(channel);
        scSkuActivityReq.setGoodsId(goodsId);

        SCSkuActivity scSkuActivity = skuActivityDao.querySCSkuActivityBySCGoodsId(scSkuActivityReq);
        if (ObjectUtils.isEmpty(scSkuActivity)) {
            return null;
        }
    // 返回查询到的 SCSkuActivityVO 对象
        return SCSkuActivityVO.builder()
                .source(scSkuActivity.getSource())
                .chanel(scSkuActivity.getChannel())
                .activityId(scSkuActivity.getActivityId())
                .goodsId(scSkuActivity.getGoodsId())
                .build();
    }

    @Override
    public boolean isTargetCrowd(String tagId, String userId) {
        log.info("检查用户是否命中标签条件，tagId：{}，userId：{}", tagId, userId);
        RBitSet bitSet = redisService.getBitSet(tagId);
        if (!bitSet.isExists()) {
            // 不存在则表示全部用户命中
            return true;
        }
        int userIdx = redisService.getIndexFromUserId(userId);
        return bitSet.get(userIdx);
    }

    @Override
    public boolean downgradeSwitch() {
        return dynamicConfigCenter.isDowngrade();
    }

    @Override
    public boolean cutRange(String userId) {
        return dynamicConfigCenter.isCutRange(userId);
    }

}
