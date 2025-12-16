package cn.hjw.dev.platform.infrastructure.dao;


import cn.hjw.dev.platform.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description 商品查询
 * @create 2024-12-21 10:48
 */
@Mapper
public interface ISkuDao {

    /**
     * 根据商品ID查询
     */
    Sku querySkuByGoodsId(String goodsId);

    /**
     * 录入新商品SKU
     */
    void insert(Sku sku);

    /**
     * 更新商品信息
     */
    int update(Sku sku);

    /**
     * 查询商品列表（后台用）
     */
    List<Sku> querySkuList();
}
