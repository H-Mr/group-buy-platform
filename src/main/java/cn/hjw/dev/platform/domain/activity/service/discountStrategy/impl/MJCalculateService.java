package cn.hjw.dev.platform.domain.activity.service.discountStrategy.impl;

import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.hjw.dev.platform.domain.activity.service.discountStrategy.AbstractDiscountCalculateService;
import cn.hjw.dev.platform.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 满减优惠计算
 * @create 2024-12-22 12:12
 */
@Slf4j
@Service("MJ")
public class MJCalculateService extends AbstractDiscountCalculateService {

    @Override
    public BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscount.getDiscountType().getCode());
        log.info("使用的优惠策略是满减");

        // 折扣表达式 - 100,10 满100减10元
        String marketExpr = groupBuyDiscount.getMarketExpr();
        String[] split = marketExpr.split(Constants.SPLIT);
        BigDecimal x = new BigDecimal(split[0].trim());
        BigDecimal y = new BigDecimal(split[1].trim());

        // 不满足最低满减约束，则按照原价
        if (originalPrice.compareTo(x) < 0) {
            return originalPrice;
        }

        // 折扣价格
        return  originalPrice.subtract(y);
    }

}
