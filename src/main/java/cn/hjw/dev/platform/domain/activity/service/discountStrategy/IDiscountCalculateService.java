package cn.hjw.dev.platform.domain.activity.service.discountStrategy;

import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;

public interface IDiscountCalculateService {

    /**
     * 计算折扣价格
     *
     * @param userId            用户ID
     * @param originalPrice    原始价格
     * @param groupBuyDiscount 折扣配置
     * @return 折扣价格
     */
    BigDecimal calculateDiscountPrice(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);
}
