package cn.hjw.dev.platform.api.dto.admin;

import lombok.Data;

// 3. 创建折扣请求
@Data
public class DiscountCreateRequestDTO {

    private String discountName;
    private String discountDesc;
    private Integer discountType;
    private String marketPlan;
    private String marketExpr;
    private String tagId;
}
