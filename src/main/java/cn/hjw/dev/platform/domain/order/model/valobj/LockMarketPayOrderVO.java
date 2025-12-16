package cn.hjw.dev.platform.domain.order.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 记录锁单的拼团订单信息
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LockMarketPayOrderVO {

    private String userId; // 下单的用户ID
    private String teamId;  // 拼单组队ID - 可为空，为空则创建新组队ID
    private Long activityId;  // 活动ID
    private String productId; // 商品ID
    private String orderId; // 订单ID
}
