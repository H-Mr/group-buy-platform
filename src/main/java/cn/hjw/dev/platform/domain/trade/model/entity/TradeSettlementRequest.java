package cn.hjw.dev.platform.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 结算 DAG 请求入参
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeSettlementRequest {
    /** 用户ID */
    private String userId;
    /** 外部交易单号 */
    private String outTradeNo;
    /** 外部交易时间 */
    private LocalDateTime outTradeTime;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
}
