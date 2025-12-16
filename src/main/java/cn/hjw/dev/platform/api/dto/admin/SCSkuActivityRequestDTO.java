package cn.hjw.dev.platform.api.dto.admin;

import lombok.Data;

// 4. 商品活动绑定请求
@Data
public class SCSkuActivityRequestDTO {
    private String source;
    private String channel;
    private String goodsId;    // 必须是已存在的 goodsId
    private Long activityId; // 必须是已存在的 activityId
}