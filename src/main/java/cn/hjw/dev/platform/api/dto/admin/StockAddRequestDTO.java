package cn.hjw.dev.platform.api.dto.admin;

import lombok.Data;

/**
 * 增加库存请求
 */
@Data
public class StockAddRequestDTO {
    private String goodsId;
    private Integer count;
}
