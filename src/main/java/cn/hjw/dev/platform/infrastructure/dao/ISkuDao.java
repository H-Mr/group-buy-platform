package cn.hjw.dev.platform.infrastructure.dao;


import cn.hjw.dev.platform.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description 商品查询
 * @create 2024-12-21 10:48
 */
@Mapper
public interface ISkuDao {

    Sku querySkuByGoodsId(String goodsId);
}
