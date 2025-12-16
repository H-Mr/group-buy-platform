package cn.hjw.dev.platform.domain.activity.service.discountStrategy.impl;

import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.service.discountStrategy.AbstractDiscountCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 满减优惠计算
 * @create 2024-12-22 12:12
 */
@Slf4j
@Service("N")
public class NCalculateService extends AbstractDiscountCalculateService {

    @Override
    public BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscount.getDiscountType().getCode());
        log.info("使用的优惠策略是n元购");

        // 折扣表达式 - 直接为优惠后的金额
        String marketExpr = groupBuyDiscount.getMarketExpr();
        // n元购
        return new BigDecimal(marketExpr);
    }

}
