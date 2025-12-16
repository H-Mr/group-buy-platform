package cn.hjw.dev.platform.infrastructure.dao;


import cn.hjw.dev.platform.infrastructure.dao.po.SCSkuActivity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 渠道商品活动配置关联表Dao
 * @create 2025-01-01 09:30
 */
@Mapper
public interface ISCSkuActivityDao {

    /**
     * 查询关联关系
     */
    SCSkuActivity querySCSkuActivityBySCGoodsId(SCSkuActivity scSkuActivity);

    /**
     * 新增绑定关系
     */
    void insert(SCSkuActivity scSkuActivity);

    /**
     * 更新绑定的活动ID
     */
    int updateActivityId(SCSkuActivity scSkuActivity);

}
