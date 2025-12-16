package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.api.IPayService;
import cn.hjw.dev.platform.api.dto.CreatePayRequestDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.domain.order.event.OrderStatusChangedEventType;
import cn.hjw.dev.platform.domain.order.model.entity.PayOrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ShopCartEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.MarketTypeVO;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import cn.hjw.dev.platform.domain.order.service.IOrderService;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.utils.UserContext;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
// tpgvta9856@sandbox.com

/**
 * 用户下单（token）
 * 负责处理支付宝支付相关的HTTP请求
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/alipay/")
public class AliPayController implements IPayService {

    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;

    @Resource
    private IOrderService orderService; // 锁单

    @Resource
    private OrderStatusChangedEventType orderStatusChangedEventType;

    // 支付宝回调时间格式（固定：yyyy-MM-dd HH:mm:ss）
    private static final DateTimeFormatter ALIPAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * 商品下单
     * @param createPayRequestDTO
     * @return 返回订单的支付地址
     */
    @RequestMapping(value = "create_pay_order", method =  RequestMethod.POST)
    @Override
    public Response<String> createPayOrder(@RequestBody CreatePayRequestDTO createPayRequestDTO) {
        String userId = UserContext.getUserId();
        try {
            log.info("商品下单，根据商品ID创建支付单开始 userId:{} productId:{}", userId, createPayRequestDTO.getProductId());
            // todo 判断参数是否有效
            String productId = createPayRequestDTO.getProductId();
            // 下单，会检验是否存在未支付订单，存在则直接返回支付地址
            PayOrderEntity payOrderEntity = orderService.createOrder(ShopCartEntity.builder()
                    .userId(userId) // 用户ID
                    .productId(productId) // 商品ID
                    .marketTypeVO(MarketTypeVO.GROUP_BUY_MARKET)
                    .activityId(createPayRequestDTO.getActivityId())
                    .teamId(createPayRequestDTO.getTeamId())
                    .build());

            log.info("商品下单，根据商品ID创建支付单完成 userId:{} productId:{} orderId:{}", userId, productId, payOrderEntity.getOrderId());
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(payOrderEntity.getPayUrl())
                    .build();
        } catch (Exception e) {
            log.error("商品下单，根据商品ID创建支付单失败 userId:{} productId:{}", userId, createPayRequestDTO.getProductId(), e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 支付宝回调接口
     * 支付宝支付结果回调接口，收到回调后发布订单支付成功事件
     * @param request http请求对象
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping(value = "alipay_notify_url", method = RequestMethod.POST)
    public String payNotify(HttpServletRequest request) throws AlipayApiException {
        log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));

        // 1. 校验交易状态：仅处理支付成功的回调
        String tradeStatus = request.getParameter("trade_status");
        if (!request.getParameter("trade_status").equals("TRADE_SUCCESS")) {
            log.warn("支付回调状态非成功，trade_status={}", tradeStatus);
            return "false";
        }

        // 2. 提取所有回调参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            params.put(name, request.getParameter(name));
        }

        // 3. 核心参数提取
        String tradeNo = params.get("out_trade_no");
        String gmtPaymentStr = params.get("gmt_payment");
        String alipayTradeNo = params.get("trade_no");

        // 4. 支付宝签名验证
        String sign = params.get("sign");
        String content = AlipaySignature.getSignCheckContentV1(params);
        boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayPublicKey, "UTF-8");
        if (!checkSignature) {
            log.error("支付回调签名验证失败，out_trade_no={}", tradeNo);
            return "false";
        }

        // 5. 日志打印回调信息
        log.info("支付回调，交易名称: {}", params.get("subject"));
        log.info("支付回调，交易状态: {}", params.get("trade_status"));
        log.info("支付回调，支付宝交易凭证号: {}", alipayTradeNo);
        log.info("支付回调，商户订单号: {}", tradeNo);
        log.info("支付回调，交易金额: {}", params.get("total_amount"));
        log.info("支付回调，买家在支付宝唯一id: {}", params.get("buyer_id"));
        log.info("支付回调，买家付款时间(原始): {}", gmtPaymentStr);
        log.info("支付回调，买家付款金额: {}", params.get("buyer_pay_amount"));

        // 6. 发布订单支付成功事件
        // todo 插入事件表 + 定时任务来补偿任务机制，保证最终一致性
        LocalDateTime paySuccessTime = LocalDateTime.parse(gmtPaymentStr, ALIPAY_TIME_FORMATTER);
        orderStatusChangedEventType.publishOrderStatusChangedEvent(tradeNo, OrderStatusVO.PAY_SUCCESS,paySuccessTime);

        return "success";
    }

}
