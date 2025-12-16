package cn.hjw.dev.platform.trigger.listener;

import cn.hjw.dev.platform.domain.inventory.event.InventoryChangedEventType;
import cn.hjw.dev.platform.infrastructure.gateway.ProductGateway;
import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
@Slf4j
public class InventoryChangedEventListener {


    @Resource(name = "sys-event-bus")
    private EventBus eventBus;

    @Resource
    private ProductGateway productGateway;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onInventoryChangedEvent(BaseEventType.Message<InventoryChangedEventType.ChangedProductInventory> event) {

        Object payload = event.getPayload();
        if (!(payload instanceof InventoryChangedEventType.ChangedProductInventory)) {
            return;
        }
        InventoryChangedEventType.ChangedProductInventory inventoryChanged = (InventoryChangedEventType.ChangedProductInventory) payload;
        log.info("接收到库存变更事件: {}", inventoryChanged);

        log.info("处理库存变更事件，商品ID: {}, 变更类型: {}, 变更数量: {}",
                inventoryChanged.getProductId(),
                inventoryChanged.getChangeType().getDesc(),
                inventoryChanged.getChangeQuantity()
        );
        productGateway.deductInventory(
                inventoryChanged.getProductId(),
                inventoryChanged.getChangeType(),
                inventoryChanged.getChangeQuantity()
        );
    }

}
