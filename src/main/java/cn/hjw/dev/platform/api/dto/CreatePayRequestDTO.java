package cn.hjw.dev.platform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreatePayRequestDTO {

    // 产品编号
    private String productId;
    // 活动ID
    private Long activityId;
    // 拼团组队ID，可为空，为空的时，则为用户首次创建拼团
    private String teamId;
    private String source; // 商品来源
    private String channel; // 商品渠道

}
