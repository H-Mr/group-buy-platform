package cn.hjw.dev.platform.domain.inventory.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum InventoryChangedTypeVO {

    INCREASE(1, "增加库存"),
    DECREASE(2, "减少库存"),
    ;

    public static InventoryChangedTypeVO valueOf(Integer code) {
        switch (code) {
            case 1:
                return INCREASE;
            case 2:
                return DECREASE;

        }
        throw new RuntimeException("err code not exist!");
    }

    private Integer code;
    private String desc;
}
