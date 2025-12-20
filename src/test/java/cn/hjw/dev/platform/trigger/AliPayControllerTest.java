package cn.hjw.dev.platform.trigger;

import cn.hjw.dev.platform.api.IPayService;
import cn.hjw.dev.platform.api.dto.CreateOrderDTO;
import cn.hjw.dev.platform.api.dto.CreatePayRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
public class AliPayControllerTest {

    @Resource
    private IPayService payService;

    @Test
    public void testCreatePayOrder() throws Exception {
        // 入参信息
        Long activityId = 100123L;
        String userId = "hhjjww11";
        String productId = "9890001";
        Integer marketType = 1;
        // 1. 商品下单，根据商品ID创建支付单
        log.info("商品下单，根据商品ID创建支付单开始 userId:{} productId:{}", userId, productId);
        CreatePayRequestDTO payRequestDTO = CreatePayRequestDTO.builder()
                .activityId(activityId)
                .productId(productId)
                .build();
        CreateOrderDTO data = payService.createPayOrder(payRequestDTO).getData();
        log.info("商品下单，根据商品ID创建支付单完成 payUrl:{}", data.getPayUrl());
    }
}
