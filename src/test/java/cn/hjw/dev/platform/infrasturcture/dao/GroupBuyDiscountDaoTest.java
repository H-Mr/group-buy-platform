package cn.hjw.dev.platform.infrasturcture.dao;

import cn.hjw.dev.platform.infrastructure.dao.IGroupBuyDiscountDao;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class GroupBuyDiscountDaoTest {

    @Autowired
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Test
    public void test(){
        log.info(JSON.toJSONString(groupBuyDiscountDao.queryGroupBuyDiscountList()));
    }
}
