package cn.hjw.dev.platform.api.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

// 2. 创建活动请求
@Data
public class ActivityCreateRequestDTO {
    private String activityName;
    private String discountId; // 关联的折扣ID（需先创建折扣）
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
