package cn.hjw.dev.platform.domain.tag;


import cn.hjw.dev.platform.domain.tag.service.ITagService;
import cn.hjw.dev.platform.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBitSet;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 人群标签服务测试
 * @create 2024-12-28 14:33
 */
@Slf4j
@SpringBootTest
public class ITagServiceTest {

    @Resource
    private ITagService tagService;
    @Resource
    private IRedisService redisService;

    /**
     * 测试执行标签任务
     */
    @Test
    public void test_tag_job() {
        tagService.executeCrowdTagsJob("RQ_KJHKL98UU78H66554GFDV", "10001");
    }

    /**
     * 测试获取标签位图,判断用户是否在标签中
     */
    @Test
    public void test_get_tag_bitmap() {
        RBitSet bitSet = redisService.getBitSet("RQ_KJHKL98UU78H66554GFDV");
        // 是否存在
        log.info("xiaofuge 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xiaofuge")));
        log.info("gudebai 不存在，预期结果为 false，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("gudebai")));
    }

}
