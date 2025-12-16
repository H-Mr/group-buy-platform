package cn.hjw.dev.platform.infrastructure.dao;


import cn.hjw.dev.platform.infrastructure.dao.po.GroupBuyOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 用户拼单
 * @create 2025-01-11 10:33
 */
@Mapper
public interface IGroupBuyOrderDao {

    /**
     * 插入用户拼单
     * @param groupBuyOrder
     */
    void insert(GroupBuyOrder groupBuyOrder);

    /**
     * 增加锁单数量
     * @param teamId
     * @return
     */
    int updateAddLockCount(String teamId);

    /**
     * 减少锁单数量
     * @param teamId
     * @return
     */
    int updateSubtractionLockCount(String teamId);

    /**
     * 查询拼单进度
     * @param teamId
     * @return
     */
    GroupBuyOrder queryGroupBuyProgress(String teamId);

    GroupBuyOrder queryGroupBuyTeamByTeamId(String teamId);

    int updateOrderStatus2COMPLETE(String teamId);

    int updateAddCompleteCount(String teamId);
}
