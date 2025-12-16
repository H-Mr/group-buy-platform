package cn.hjw.dev.platform.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeLockRequestEntity {

    private String userId; // 请求锁单的用户ID
    private Long activityId; // 锁单商品的参与的活动ID
    private String goodsId; // 锁单商品的商品ID
    private String source;  // 商品来源
    private String channel; // 商品渠道
    private String outTradeNo; // 当前商品下单的外部交易号
    private String teamId; // 拼团ID，如果是拼团商品，则需要传递该参数
    private String notifyUrl; // 拼团回调通知地址
}
