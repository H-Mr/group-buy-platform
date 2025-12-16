package cn.hjw.dev.platform.infrastructure.dao;


import cn.hjw.dev.platform.infrastructure.dao.po.CrowdTags;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 人群标签
 * @create 2024-12-28 11:49
 */
@Mapper
public interface ICrowdTagsDao {

    void updateCrowdTagsStatistics(CrowdTags crowdTagsReq);

}
