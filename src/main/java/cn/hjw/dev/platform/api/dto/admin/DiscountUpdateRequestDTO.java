package cn.hjw.dev.platform.api.dto.admin;

import lombok.Data;

@Data
public class DiscountUpdateRequestDTO {
    private String discountId;    // 必须，作为唯一标识
    private String discountName;
    private String discountDesc;
    private Integer discountType;
    private String marketPlan;
    private String marketExpr;
    private String tagId;
}