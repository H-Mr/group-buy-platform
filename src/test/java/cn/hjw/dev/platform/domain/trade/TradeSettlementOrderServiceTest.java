package cn.hjw.dev.platform.domain.trade;

import cn.hjw.dev.platform.domain.trade.model.entity.TradePaySettlementEntity;
import cn.hjw.dev.platform.domain.trade.model.entity.TradePaySuccessEntity;
import cn.hjw.dev.platform.domain.trade.service.ITradeSettlementOrderService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 拼团交易结算服务测试
 * @create 2025-01-26 18:59
 */
@Slf4j
@SpringBootTest
public class TradeSettlementOrderServiceTest {

    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;

    @Test
    public void test_settlementMarketPayOrder() throws Exception {

        // 入参信息
        Long activityId = 100123L;
        String userId = "hhjjww4";
        String goodsId = "9890001";
        String source = "s01";
        String channel = "c01";
        String outTradeNo = "80900009821";
        String notifyUrl = "http://www.test.com/notify";
        String teamId = "09065583";


        TradePaySuccessEntity tradePaySuccessEntity = new TradePaySuccessEntity();
        tradePaySuccessEntity.setUserId(userId);
        tradePaySuccessEntity.setOutTradeNo(outTradeNo);
        tradePaySuccessEntity.setSource(source);
        tradePaySuccessEntity.setChannel(channel);
        tradePaySuccessEntity.setOutTradeTime(LocalDateTime.now()); // 设置当前时间
        TradePaySettlementEntity tradePaySettlementEntity = tradeSettlementOrderService.settlementMarketPayOrder(tradePaySuccessEntity);
        log.info("请求参数:{}", JSON.toJSONString(tradePaySuccessEntity));
        log.info("测试结果:{}", JSON.toJSONString(tradePaySettlementEntity));
    }

}
