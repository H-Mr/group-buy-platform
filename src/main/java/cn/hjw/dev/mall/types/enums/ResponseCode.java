package cn.hjw.dev.mall.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("E0000", "成功"),
    UN_ERROR("E0001", "未知失败"),
    ILLEGAL_PARAMETER("E0002", "非法参数"),
    ILLEGAL_DISCOUNT_SERVICE("E0003", "非法折扣服务"),
    ILLEGAL_MARKET_CONFIG("E0004", "非法拼团活动配置"),
    DOWNGRADE("E0005", "拼团活动降级拦截"),
    CNTRANGE("E0006", "拼团活动切量拦截"),
    INDEX_EXCEPTION("E0009", "唯一索引冲突"),
    BIZ_ERROR("E0014", "非法的拼团订单"),
    UPDATE_ZERO("0004", "更新记录为0"),
    HTTP_EXCEPTION("0005", "HTTP接口调用异常"),

    E0005("E0007", "拼团组队失败，记录更新为0"),
    E0006("E0008", "拼团组队完结，锁单量已达成"),
    E0007("E0010", "拼团人群限定，不可参与"),

    E0101("E0011", "拼团活动未生效"),
    E0102("E0012", "不在拼团活动有效时间内"),
    E0103("E0013", "当前用户参与此拼团次数已达上限"),

    E0104("E0104", "不存在的外部交易单号或用户已退单"),
    E0105("E0105", "SC渠道黑名单拦截"),
    E0106("E0106", "订单交易时间不在拼团有效时间范围内"),    ;

    private String code;
    private String info;

}
