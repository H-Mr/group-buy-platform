package cn.hjw.dev.platform.infrastructure.dao;

import cn.hjw.dev.platform.infrastructure.dao.po.SkuStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ISkuStockDao {
    /**
     * 原子性扣减库存
     * update sku_stock set stock_count = stock_count - count where goods_id = ? and stock_count >= count
     */
    int decrementStock(@Param("goodsId") String goodsId, @Param("count") Integer count);

    void insert(SkuStock skuStock);

    // 增加库存（回滚或补货用）
    int increaseStock(@Param("goodsId") String goodsId, @Param("count") Integer count);

    SkuStock queryByGoodsId(String goodsId);
}
