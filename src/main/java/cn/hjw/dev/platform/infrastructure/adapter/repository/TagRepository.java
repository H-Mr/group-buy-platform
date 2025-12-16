package cn.hjw.dev.platform.infrastructure.adapter.repository;

import cn.hjw.dev.platform.domain.tag.adapter.repository.ITagRepository;
import cn.hjw.dev.platform.domain.tag.model.entity.CrowdTagsJobEntity;
import cn.hjw.dev.platform.infrastructure.dao.ICrowdTagsDao;
import cn.hjw.dev.platform.infrastructure.dao.ICrowdTagsDetailDao;
import cn.hjw.dev.platform.infrastructure.dao.ICrowdTagsJobDao;
import cn.hjw.dev.platform.infrastructure.dao.po.CrowdTags;
import cn.hjw.dev.platform.infrastructure.dao.po.CrowdTagsDetail;
import cn.hjw.dev.platform.infrastructure.dao.po.CrowdTagsJob;
import cn.hjw.dev.platform.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBitSet;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TagRepository implements ITagRepository {

    @Resource
    private ICrowdTagsDao crowdTagsDao; // 人群标签DAO
    @Resource
    private ICrowdTagsDetailDao crowdTagsDetailDao; // 人群标签明细DAO
    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao; // 人群标签任务DAO

    @Resource
    private IRedisService redisService; // Redis服务

    /**
     * 查询人群标签任务实体
     * @param tagId
     * @param batchId
     * @return
     */
    @Override
    public CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId) {

        log.info("查询人群标签任务实体 tagId:{} batchId:{}", tagId, batchId);

        CrowdTagsJob crowdTagsJobReq = new CrowdTagsJob(); // 创建请求对象
        crowdTagsJobReq.setTagId(tagId);
        crowdTagsJobReq.setBatchId(batchId);

        CrowdTagsJob crowdTagsJobRes = crowdTagsJobDao.queryCrowdTagsJob(crowdTagsJobReq); // DAO查询标签任务
        if (null == crowdTagsJobRes) return null; // 如果结果为空，返回null

        return CrowdTagsJobEntity.builder()
                .tagType(crowdTagsJobRes.getTagType())
                .tagRule(crowdTagsJobRes.getTagRule())
                .statStartTime(crowdTagsJobRes.getStatStartTime())
                .statEndTime(crowdTagsJobRes.getStatEndTime())
                .build();
    }

    /**
     * 添加人群标签用户ID
     * @param tagId
     * @param userId
     */
    @Override
    public void addCrowdTagsUserId(String tagId, String userId) {

        log.info("添加人群标签用户ID tagId:{} userId:{}", tagId, userId);
        CrowdTagsDetail crowdTagsDetailReq = new CrowdTagsDetail(); // 创建人群标签明细请求对象
        crowdTagsDetailReq.setTagId(tagId); // 设置标签ID
        crowdTagsDetailReq.setUserId(userId); // 设置用户ID

        try {
            crowdTagsDetailDao.addCrowdTagsUserId(crowdTagsDetailReq);

            // 获取BitSet
            RBitSet bitSet = redisService.getBitSet(tagId); // 从Redis服务获取BitSet
            // 设置用户ID对应的索引位置为true
            bitSet.set(redisService.getIndexFromUserId(userId), true); // 把用户id映射到BitSet索引位置
        } catch (DuplicateKeyException ignore) {
            // 忽略唯一索引冲突
        }
    }

    /**
     * 更新人群标签统计量
     * @param tagId
     * @param count
     */
    @Override
    public void updateCrowdTagsStatistics(String tagId, int count) {
        log.info("更新人群标签统计量 tagId:{} count:{}", tagId, count);
        CrowdTags crowdTagsReq = new CrowdTags();
        crowdTagsReq.setTagId(tagId);
        crowdTagsReq.setStatistics(count);

        crowdTagsDao.updateCrowdTagsStatistics(crowdTagsReq);
    }

}
