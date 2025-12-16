package cn.hjw.dev.platform.domain.activity;


import cn.hjw.dev.platform.domain.activity.model.entity.MarketProductEntity;
import cn.hjw.dev.platform.domain.activity.model.entity.TrialBalanceEntity;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 动态值变更
 * <p>
 * curl http://127.0.0.1:8091/api/v1/gbm/dcc/update_config?key=downgradeSwitch&value=1
 * curl http://127.0.0.1:8091/api/v1/gbm/dcc/update_config?key=cutRange&value=0
 * @description 首页营销服务接口测试
 * @create 2024-12-21 11:08
 */
@Slf4j
@SpringBootTest
public class IIndexGroupBuyMarketServiceTest {

    @Resource
    private IGroupBuyMarketService indexGroupBuyMarketService;

    @Test
    public void test_indexMarketTrial() throws Exception {
        MarketProductEntity marketProductEntity = new MarketProductEntity();
        marketProductEntity.setUserId("xiaofuge");
        marketProductEntity.setSource("s01");
        marketProductEntity.setChannel("c01");
        marketProductEntity.setGoodsId("9890001");

        TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(marketProductEntity);
        log.info("请求参数:{}", JSON.toJSONString(marketProductEntity));
        log.info("返回结果:{}", JSON.toJSONString(trialBalanceEntity));
    }

}
