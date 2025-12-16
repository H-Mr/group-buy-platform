package cn.hjw.dev.platform.domain.order.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 记录锁单商品的价格信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMarketProductPriceVO {

        /** 原始价格 */
        private BigDecimal originalPrice;
        /** 折扣金额 */
        private BigDecimal deductionPrice;
        /** 支付金额 */
        private BigDecimal payPrice;
}
