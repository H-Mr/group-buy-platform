package cn.hjw.dev.platform.domain.activity.service.discountStrategy;

import cn.hjw.dev.platform.domain.activity.adapter.repository.IActivityRepository;
import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.types.common.Constants;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
public abstract class AbstractDiscountCalculateService implements IDiscountCalculateService {

    @Resource
    private IActivityRepository activityRepository;

    /**
     * 公共的计算折扣价格方法，可以包含一些通用逻辑，人群过滤，保证折扣价格不为负等
     *
     * @param userId         用户ID
     * @param originalPrice  原始价格
     * @param groupBuyDiscount 折扣配置
     * @return 折扣价格
     */
    @Override
    public BigDecimal calculateDiscountPrice(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        // 判断折扣类型是否为标签折扣
//        if(groupBuyDiscount.getDiscountType().equals(DiscountTypeEnum.TAG)) {
//            // 会员折扣，判断用户是否满足条件
//            boolean isValidUser = fillerUserTagCondition(groupBuyDiscount.getTagId(),userId);
//            if (!isValidUser) {
//                log.info("用户不满足标签条件，无法享受该折扣，userId：{}，tagId：{}", userId, groupBuyDiscount.getTagId());
//                // 用户不满足标签条件，不适用该折扣，返回原价
//                return originalPrice;
//            }
//        }
        BigDecimal deducePrice = doCalculate(originalPrice, groupBuyDiscount);
        if (deducePrice.compareTo(BigDecimal.ZERO) <= 0) {
            // 保证折扣价格不为负，最低支付MIN_PRICE
            deducePrice = Constants.MIN_PRICE;
        }
        return deducePrice;
    }


    /**
     * 填充用户标签条件，判断用户是否满足特定标签条件
     *
     * @param userId 用户ID
     * @param tagId  标签ID
     * @return 是否满足条件
     */
    protected boolean fillerUserTagCondition(String userId, String tagId) {
        return activityRepository.isTargetCrowd(userId, tagId);
    }

    /**
     * 抽象方法，由子类实现具体的折扣计算逻辑
     *
     * @param originalPrice  原始价格
     * @param groupBuyDiscount 折扣配置
     * @return 折扣价格
     */
    public abstract BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);
}
