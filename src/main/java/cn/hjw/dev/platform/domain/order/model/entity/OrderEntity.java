package cn.hjw.dev.platform.domain.order.model.entity;

import cn.hjw.dev.platform.domain.order.model.valobj.MarketTypeVO;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {

    private Long id;  // 主键ID
    private String orderId; // 订单ID
    private String userId; // 下单用户ID
    private MarketTypeVO marketType; // 营销类型枚举 NO_MARKET GROUP_BUY_MARKET
    private String productId; // 商品ID
    private String productName; // 商品名称
    private BigDecimal totalAmount; // 订单总金额
    private BigDecimal marketDeductionAmount; // 营销金额；优惠金额
    private BigDecimal payAmount;  // 支付金额
    private OrderStatusVO orderStatusVO; // 订单状态枚举
    private String payUrl; // 支付链接(支付表单)
    private LocalDateTime orderTime; // 订单创建时间
    private LocalDateTime payTime; // 支付时间


}
