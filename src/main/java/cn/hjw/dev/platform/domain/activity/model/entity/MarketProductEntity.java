package cn.hjw.dev.platform.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 营销商品实体对象，用于计算拼团活动商品的折扣价格
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketProductEntity {

    /** 用户ID */
    private String userId;
    /** 商品ID */
    private String goodsId;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 活动ID */
    private Long activityId;

}
