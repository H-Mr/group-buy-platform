package cn.hjw.dev.platform.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCoupon {
    private Long id;
    private String userId;
    private String couponId;
    private Integer status; // 0-未使用
    private String orderId;
    private Date createTime;
    private Date useTime;
}
