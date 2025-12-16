package cn.hjw.dev.platform.domain.activity.service.discountStrategy.impl;

import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.service.discountStrategy.AbstractDiscountCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 折扣优惠计算
 * @create 2024-12-22 12:12
 */
@Slf4j
@Service("ZK")
public class ZKCalculateService extends AbstractDiscountCalculateService {

    @Override
    public BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscount.getDiscountType().getCode());
        log.info("使用的优惠策略为：ZJ - 折扣");

        // 折扣表达式 - 折扣百分比
        String marketExpr = groupBuyDiscount.getMarketExpr();

        // 折扣价格
        return originalPrice.multiply(new BigDecimal(marketExpr));
    }

}
