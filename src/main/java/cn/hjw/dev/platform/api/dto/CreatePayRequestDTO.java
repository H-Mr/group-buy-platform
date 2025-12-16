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

    // 用户ID 【实际产生中会通过登录模块获取token,解析userId，不需要透彻】
    private String userId;
    // 产品编号
    private String productId;
    // 营销类型 0-无营销 1-拼团
    private Integer marketType;
    // 活动ID
    private Long activityId;
    // 拼团组队ID，可为空，为空的时，则为用户首次创建拼团
    private String teamId;

}
