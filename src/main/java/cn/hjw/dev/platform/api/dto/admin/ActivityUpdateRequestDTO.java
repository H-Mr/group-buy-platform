package cn.hjw.dev.platform.api.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 更新活动请求
 */
@Data
public class ActivityUpdateRequestDTO {
    private Long activityId;       // 必须传
    private String activityName;
    private String discountId;
    private Integer groupType;
    private Integer takeLimitCount;
    private Integer target;
    private Integer validTime;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String tagId;
    private String tagScope;
}
