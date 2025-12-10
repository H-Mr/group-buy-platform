package cn.hjw.dev.mall.infrastructure.dao;

import cn.hjw.dev.infrastructure.dao.po.GroupBuyActivity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 拼团活动Mapper接口
 */
@Mapper
public interface IGroupBuyActivityDao {

    /**
     * 查询所有拼团活动
     */
    List<GroupBuyActivity> queryGroupBuyActivityList();

    /**
     * 查询有效的拼团活动
     */
    GroupBuyActivity queryValidGroupBuyActivity(GroupBuyActivity groupBuyActivityReq);

    /**
     * 根据活动ID查询有效的拼团活动
     */
    GroupBuyActivity queryValidGroupBuyActivityId(Long activityId);

    /**
     * 根据活动ID查询拼团活动
     */
    GroupBuyActivity queryGroupBuyActivityByActivityId(Long activityId);
}