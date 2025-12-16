package cn.hjw.dev.platform.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /** 自增ID */
    private Long id;
    /** 系统用户ID */
    private String userId;
    /** 微信的openID */
    private String openid;

    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
