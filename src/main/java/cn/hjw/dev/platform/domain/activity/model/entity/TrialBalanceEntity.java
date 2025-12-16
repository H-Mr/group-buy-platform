package cn.hjw.dev.platform.domain.activity.model.entity;

import cn.hjw.dev.platform.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 这是服务的返参对象。它不是最终的订单，而是给用户看的“预演结果”（Trial Balance 即试算平衡）。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrialBalanceEntity {
    /** 商品ID */
    private String goodsId;
    /** 商品名称 */
    private String goodsName;
    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 折扣价格 */
    private BigDecimal deductionPrice;
    /** 支付价格 */
    private  BigDecimal payPrice;
    /** 拼团目标数量 */
    private Integer targetCount;
    /** 拼团开始时间 */
    private LocalDateTime startTime;
    /** 拼团结束时间 */
    private LocalDateTime endTime;
    /** 是否可见拼团 */
    private Boolean isVisible;
    /** 是否可参与进团 */
    private Boolean isEnable;
    /** 活动折扣信息 */
    private GroupBuyActivityDiscountVO groupBuyActivityDiscountVO;
}
