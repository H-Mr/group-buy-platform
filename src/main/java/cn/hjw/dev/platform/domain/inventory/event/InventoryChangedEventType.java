package cn.hjw.dev.platform.domain.inventory.event;

import cn.hjw.dev.platform.domain.inventory.model.valobj.InventoryChangedTypeVO;
import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;



@Component
public class InventoryChangedEventType extends BaseEventType<InventoryChangedEventType.ChangedProductInventory> {

    @Resource(name = "sys-event-bus")
    private EventBus eventBus; // 注入 Guava EventBus

    /**
     * 发布库存变更事件
     * @param productId 商品ID
     * @param changeType 库存变更类型
     * @param changeQuantity 库存变更数量
     */
    public void publishInventoryChangedEvent(String productId, InventoryChangedTypeVO changeType, Integer changeQuantity) {
        ChangedProductInventory payload = ChangedProductInventory.builder()
                .productId(productId)
                .changeType(changeType)
                .changeQuantity(changeQuantity)
                .build();
        Message<ChangedProductInventory> eventMessage = buildEventPayload(payload);
        eventBus.post(eventMessage);
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChangedProductInventory {

        InventoryChangedTypeVO changeType; // 库存变更类型
        String productId; // sku商品ID
        Integer changeQuantity; // 库存变更数量
    }
}
