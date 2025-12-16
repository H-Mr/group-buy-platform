package cn.hjw.dev.platform.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 请求：只需传 teamId，可为空
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifyRequestEntity {

    private String teamId;
}
