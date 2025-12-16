package cn.hjw.dev.platform.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 用户拼单明细
 * @create 2025-01-11 08:42
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyOrderList {

    /** 自增ID */
    private Long id;
    /** 用户ID */
    private String userId;
    /** 拼单组队ID */
    private String teamId;
    /** 订单ID */
    private String orderId;
    /** 活动ID */
    private Long activityId;
    /** 活动开始时间 */
    private LocalDateTime startTime;
    /** 活动结束时间 */
    private LocalDateTime endTime;
    /** 商品ID */
    private String goodsId;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 折扣金额 */
    private BigDecimal deductionPrice;
    /** 状态；0初始锁定、1消费完成 */
    private Integer status;
    /** 唯一业务ID */
    private String bizId;
    /** 外部交易时间 */
    private LocalDateTime outTradeTime;
    /** 外部交易单号-确保外部调用唯一幂等 */
    private String outTradeNo;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

}
