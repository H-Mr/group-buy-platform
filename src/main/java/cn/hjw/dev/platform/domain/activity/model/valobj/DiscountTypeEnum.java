package cn.hjw.dev.platform.domain.activity.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiscountTypeEnum {

    BASE(0, "基础折扣"),
    TAG(1, "会员折扣"),
    ;
    private final Integer code;
    private final String description;

    public static DiscountTypeEnum getType(Integer code) {
        // DiscountTypeEnum.values() 返回枚举类的所有值
        for (DiscountTypeEnum discountTypeEnum : DiscountTypeEnum.values()) {
            if (discountTypeEnum.getCode().equals(code)) {
                return discountTypeEnum;
            }
        }
        throw new RuntimeException("未知的折扣类型: " + code);
    }
}
