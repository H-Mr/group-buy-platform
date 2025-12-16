package cn.hjw.dev.platform.domain.activity.service.discountStrategy.impl;

import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.service.discountStrategy.AbstractDiscountCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 直减
 * @create 2024-12-22 09:24
 */
@Slf4j
@Service("ZJ")
public class ZJCalculateService extends AbstractDiscountCalculateService {

    @Override
    public BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscount.getDiscountType().getCode());
        log.info("使用的优惠策略是直减");

        // 折扣表达式 - 直减为扣减金额
        String marketExpr = groupBuyDiscount.getMarketExpr();

        return originalPrice.subtract(new BigDecimal(marketExpr));
    }

}
