package cn.hjw.dev.platform.domain.activity.adapter.repository;


import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.model.valobj.SCSkuActivityVO;
import cn.hjw.dev.platform.domain.activity.model.valobj.SkuVO;

/**
 * @description 活动仓储
 * @create 2024-12-21 10:06
 */
public interface IActivityRepository {

    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId);

    SkuVO querySkuByGoodsId(String goodsId);

    /**
     * 查询渠道商品活动配置关联配置
     */
    SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId);

    boolean isTargetCrowd(String tagId, String userId);

    boolean downgradeSwitch();

    boolean cutRange(String userId);

}
