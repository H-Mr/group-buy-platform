package cn.hjw.dev.platform.infrasturcture.dao;

import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyActivityDao;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class GroupBuyActivityDaoTest {

    @Autowired
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Test
    public void test(){
        log.info("测试结果：{}",JSON.toJSONString(groupBuyActivityDao.queryGroupBuyActivityList()));
    }
}
