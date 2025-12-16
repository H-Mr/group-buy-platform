package cn.hjw.dev.platform.api.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新商品请求
 */
@Data
public class SkuUpdateRequestDTO {
    private String goodsId;       // 必须传，用于定位
    private String goodsName;     // 可选更新
    private BigDecimal originalPrice; // 可选更新
}
