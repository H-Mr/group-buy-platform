package cn.hjw.dev.platform.infrastructure.gateway.mock;

import cn.hjw.dev.platform.domain.inventory.model.valobj.InventoryChangedTypeVO;
import cn.hjw.dev.platform.infrastructure.dao.ISkuDao;
import cn.hjw.dev.platform.infrastructure.dao.po.Sku;
import cn.hjw.dev.platform.infrastructure.gateway.ProductGateway;
import cn.hjw.dev.platform.infrastructure.gateway.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Service
public class ProductServiceMockImpl implements ProductGateway {

    @Resource
    private ISkuDao skuDao;

    /**
     * 查询固定的SKU商品信息
     * @param productId
     * @return
     */
    public ProductDTO queryProductByProductId(String productId){
        Sku sku = skuDao.querySkuByGoodsId(productId);
        if (sku == null)
            return null;
        ProductDTO productVO = new ProductDTO();
        productVO.setProductId(productId);
        productVO.setProductName(sku.getGoodsName());
        productVO.setProductDesc(sku.getGoodsName());
        productVO.setPrice(sku.getOriginalPrice());
        return productVO;
    }

    @Override
    public void deductInventory(String productId, InventoryChangedTypeVO changeType, Integer changeQuantity) {
        log.info("执行扣减库存操作，商品ID: {}, 变更类型: {}, 变更数量: {}",
                productId,
                changeType.getDesc(),
                changeQuantity
        );
    }

}
