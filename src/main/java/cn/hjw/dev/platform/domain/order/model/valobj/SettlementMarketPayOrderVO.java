package cn.hjw.dev.platform.domain.order.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 记录锁单的拼团订单信息
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SettlementMarketPayOrderVO {

    private String userId; // 下单的用户ID
    private String orderId; // 订单ID
    private LocalDateTime tradeTime; // 交易时间
    private String source; // 来源
    private String channel; // 渠道
}
