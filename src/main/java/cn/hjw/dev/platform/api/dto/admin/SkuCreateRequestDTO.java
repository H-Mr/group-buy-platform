package cn.hjw.dev.platform.api.dto.admin;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// 1. 创建商品请求
@Data
public class SkuCreateRequestDTO {
    private String goodsName;
    private BigDecimal originalPrice;
    private String source;
    private String channel;
}
