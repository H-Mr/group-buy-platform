package cn.hjw.dev.platform.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {
    private String couponId;
    private String couponName;
    private Integer discountType; // 0-直减 1-折扣
    private BigDecimal discountAmount;
    private BigDecimal minLimit;
    private Date createTime;
}