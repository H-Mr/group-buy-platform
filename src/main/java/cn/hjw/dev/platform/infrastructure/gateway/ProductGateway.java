package cn.hjw.dev.platform.infrastructure.gateway;

import cn.hjw.dev.platform.domain.inventory.model.valobj.InventoryChangedTypeVO;
import cn.hjw.dev.platform.infrastructure.gateway.dto.ProductDTO;

public interface ProductGateway {

    ProductDTO queryProductByProductId(String productId);

    void deductInventory(String productId, InventoryChangedTypeVO changeType, Integer changeQuantity);
}
