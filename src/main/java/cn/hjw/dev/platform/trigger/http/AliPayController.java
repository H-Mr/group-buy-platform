package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.api.IPayService;
import cn.hjw.dev.platform.api.dto.CreateOrderDTO;
import cn.hjw.dev.platform.api.dto.CreatePayRequestDTO;
import cn.hjw.dev.platform.api.dto.PaySuccessNotifyDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.domain.order.event.OrderStatusChangedEventType;
import cn.hjw.dev.platform.domain.order.model.entity.PayOrderEntity;
import cn.hjw.dev.platform.domain.order.model.entity.ShopCartEntity;
import cn.hjw.dev.platform.domain.order.model.valobj.MarketTypeVO;
import cn.hjw.dev.platform.domain.order.model.valobj.OrderStatusVO;
import cn.hjw.dev.platform.domain.order.service.IOrderService;
import cn.hjw.dev.platform.infrastructure.dcc.DynamicConfigCenter;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.platform.types.utils.UserContext;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
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

    @Value("${app.config.group-buy-market.source}")
    private String source;
    @Value("${app.config.group-buy-market.chanel}")
    private String chanel;

    @Resource
    private IOrderService orderService; // 锁单

    @Resource
    private OrderStatusChangedEventType orderStatusChangedEventType;

    @Resource
    private DynamicConfigCenter dynamicConfigCenter;

    // 支付宝回调时间格式（固定：yyyy-MM-dd HH:mm:ss）
    private static final DateTimeFormatter ALIPAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * 商品下单
     * @param createPayRequestDTO
     * @return 返回订单的支付地址
     */
    @RequestMapping(value = "create_pay_order", method =  RequestMethod.POST)
    @Override
    public Response<CreateOrderDTO> createPayOrder(@RequestBody CreatePayRequestDTO createPayRequestDTO) throws Exception {
        // 进行DCC校验
        if(dynamicConfigCenter.isDowngrade()) {
            throw new AppException(ResponseCode.SUCCESS.getCode(), "系统繁忙，请稍后再试");
        }

        String userId = UserContext.getUserId();
        log.info("商品下单，根据商品ID创建支付单开始 userId:{} productId:{}", userId, createPayRequestDTO.getProductId());
        String productId = createPayRequestDTO.getProductId();
        Long activityId = createPayRequestDTO.getActivityId();
        String dtoSource = createPayRequestDTO.getSource();
        String dtoChannel = createPayRequestDTO.getChannel();
        String teamId = createPayRequestDTO.getTeamId();
        if(StringUtils.isBlank(productId) || ObjectUtils.isEmpty(activityId)) {
            log.info("商品下单，根据商品ID创建支付单失败，参数无效 userId:{} productId:{}", userId, productId);
            throw  new AppException(ResponseCode.UN_ERROR.getCode(), "参数无效");
        }
        // 补充渠道，来源
        if (StringUtils.isAnyBlank(dtoSource,dtoChannel)) {
            dtoSource = source;
            dtoChannel = chanel;
        }
        // 判断是否切量
        if (!dynamicConfigCenter.isCutRange(userId)) {
            log.info("商品下单，根据商品ID创建支付单失败，系统切量 userId:{} productId:{}", userId, productId);
            throw new AppException(ResponseCode.SUCCESS.getCode(), "当前访问人数较多，请稍后再试");
        }
        // 黑名单
        if(dynamicConfigCenter.isSCBlackIntercept(dtoSource, dtoChannel)) {
            log.info("商品下单，根据商品ID创建支付单失败，系统切量或黑名单拦截 userId:{} productId:{}", userId, productId);
            throw new AppException(ResponseCode.SUCCESS.getCode(), "当前商品的source 或 channel不可用，请联系管理员");
        }
        // 下单，会检验是否存在未支付订单，存在则直接返回支付地址
        PayOrderEntity payOrderEntity = orderService.createOrder(ShopCartEntity.builder()
                .userId(userId) // 用户ID
                .productId(productId) // 商品ID
                .marketTypeVO(MarketTypeVO.GROUP_BUY_MARKET)
                .activityId(activityId)
                .teamId(teamId)
                .source(dtoSource)
                .channel(dtoChannel)
                .build());
        log.info("商品下单，根据商品ID创建支付单完成 userId:{} productId:{} orderId:{}", userId, productId, payOrderEntity.getOrderId());
        return Response.<CreateOrderDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(new CreateOrderDTO(payOrderEntity.getPayUrl(),payOrderEntity.getTeamId()))
                .build();

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
        try {
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

            // 1. 获取透传参数
            String passbackParams = request.getParameter("passback_params");

            // 3. 解析参数 (例如手动解析字符串 "source=s01&channel=c01")
            String source = null;
            String channel = null;
            if (StringUtils.isNotBlank(passbackParams)) {
                // 2. 解码 (以防万一，虽然request.getParameter通常会自动解码，但支付宝有时候会返回编码过的)
                // 如果你发现取出来的是 %73%6F... 这种格式，就需要解码；如果是 source=s01... 则不需要。
                // 建议：打印日志观察一下，通常 request.getParameter 获取到的已经是解码后的字符串
                log.info("接收到透传参数：{}", passbackParams); // source%3Ds01%26channel%3Dc01
                passbackParams = java.net.URLDecoder.decode(passbackParams, StandardCharsets.UTF_8.toString());
                // 简单的解析逻辑
                String[] scValue = passbackParams.split("&");
                for (String val : scValue) {
                    String[] kv = val.split("=");
                    if (kv.length == 2) {
                        if ("source".equals(kv[0])) source = kv[1];
                        if ("channel".equals(kv[0])) channel = kv[1];
                    }
                }
                log.info("解析结果 - source: {}, channel: {}", source, channel);
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
            log.info("回调参数 - source: {}, channel: {}", source, channel);

            // 6. 发布订单支付成功事件
            // todo 插入事件表 + 定时任务来补偿任务机制，保证最终一致性
            LocalDateTime paySuccessTime = LocalDateTime.parse(gmtPaymentStr, ALIPAY_TIME_FORMATTER);
            PaySuccessNotifyDTO paySuccessNotifyDTO = PaySuccessNotifyDTO.builder()
                    .orderStatusVO(OrderStatusVO.PAY_SUCCESS)
                    .payTime(paySuccessTime)
                    .source(source)
                    .channel(channel)
                    .tradeNo(tradeNo)
                    .build();

            orderStatusChangedEventType.publishOrderStatusChangedEvent(paySuccessNotifyDTO);
        } catch (Exception e) {
            log.error("支付回调处理异常", e);
            return "false";
        }

        return "success";
    }

}
