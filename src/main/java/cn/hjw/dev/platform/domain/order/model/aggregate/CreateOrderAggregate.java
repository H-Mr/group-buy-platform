package cn.hjw.dev.platform.domain.order.model.aggregate;

import cn.hjw.dev.platform.domain.order.model.entity.OrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ProductEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ShopCartEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderAggregate {

    private String userId;

    private ProductEntity productEntity;

    private OrderEntity orderEntity;

    /**
     * 构建订单实体的基本信息
    * @param product 订单包含的商品信息
    * @param cart 购物车信息
    * @return 订单实体
     */
    public static OrderEntity buildOrderEntity(ProductEntity product, ShopCartEntity cart){
        return OrderEntity.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .totalAmount(product.getPrice())
                .marketDeductionAmount(BigDecimal.ZERO) // 默认没有优惠金额
                .payAmount(product.getPrice()) // 默认支付金额=总金额-优惠金额
                .orderId(RandomStringUtils.randomNumeric(16)) // 订单ID，随机生成16位数字
                .orderTime(LocalDateTime.now()) // 订单创建时间
                .orderStatusVO(OrderStatusVO.CREATE) // 订单状态：已创建
                .marketType(cart.getMarketTypeVO())
                .userId(cart.getUserId())
                .build();
    }

}
