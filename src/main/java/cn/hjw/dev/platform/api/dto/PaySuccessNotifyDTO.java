package cn.hjw.dev.platform.api.dto;

import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaySuccessNotifyDTO {

    private OrderStatusVO orderStatusVO;  // 订单状态对象
    private LocalDateTime payTime;      // 支付时间
    private String source;        // SKU 来源
    private String channel;      // SKU 渠道
    private String tradeNo;      // 第三方支付交易号


}
