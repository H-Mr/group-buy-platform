package cn.hjw.dev.platform.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 人群标签明细
 * @create 2024-12-28 11:43
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrowdTagsDetail {

    /** 自增ID */
    private Long id;
    /** 人群ID */
    private String tagId;
    /** 用户ID */
    private String userId;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

}
