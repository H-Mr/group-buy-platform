package cn.hjw.dev.platform.domain.trade;

import cn.hjw.dev.platform.domain.activity.IGroupBuyMarketService;
import cn.hjw.dev.platform.domain.trade.model.entity.*;
import cn.hjw.dev.platform.domain.trade.service.ITradeLockOrderService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 交易订单服务测试
 * @create 2025-01-11 11:52
 */
@Slf4j
@SpringBootTest
public class ITradeLockOrderServiceTest {

    @Resource
    private IGroupBuyMarketService indexGroupBuyMarketService;

    @Resource
    private ITradeLockOrderService tradeOrderService;

    @Test
    public void test_lockMarketPayOrder() throws Exception {
        // 入参信息
        Long activityId = 100123L;
        String userId = "hhjjww4";
        String goodsId = "9890001";
        String source = "s01";
        String channel = "c01";
        String outTradeNo = "80900009821";
        String notifyUrl = "http://www.test.com/notify";
        String teamId = "09065583";

        TradeLockRequestEntity tradeLockRequestEntity = TradeLockRequestEntity.builder()
                .userId(userId)
                .goodsId(goodsId)
                .source(source)
                .channel(channel)
                .outTradeNo(outTradeNo)
                .activityId(activityId)
                .notifyUrl(notifyUrl)
                .teamId(teamId)
                .build();
        // 2. 锁定，营销预支付订单；商品下单前，预购锁定。
        MarketPayOrderEntity payOrderEntity = tradeOrderService.lockMarketPayOrder(tradeLockRequestEntity);

        log.info("测试结果(New):{}",JSON.toJSONString(payOrderEntity));
    }

}
