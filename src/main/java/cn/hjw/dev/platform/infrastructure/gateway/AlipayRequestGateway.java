package cn.hjw.dev.platform.infrastructure.gateway;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Service
public class AlipayRequestGateway {

    @Resource
    private AlipayClient alipayClient;

    @Value("${alipay.notify_url}")
    private String notifyUrl;

    @Value("${alipay.return_url}")
    private String returnUrl;

    /**
     * 创建支付宝电脑网站支付订单
     * @param orderId 商户订单号（唯一）
     * @param payAmount 支付金额（元，精确到分）
     * @param productName 订单标题
     * @return 支付宝返回的表单（前端渲染后跳转支付）
     */
    public String createPagePayOrder(String orderId, BigDecimal payAmount, String productName) {
        try {
            log.info("创建支付宝支付订单，orderId：{}，payAmount：{}，productName：{}", orderId, payAmount, productName);
            // 1. 创建支付请求对象
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            // 异步通知地址（支付结果回调）
            request.setNotifyUrl(notifyUrl);
            // 同步跳转地址（支付完成后前端跳转）
            request.setReturnUrl(returnUrl);

            // 2. 构造业务参数（必须严格按支付宝规范，参数名不能错）
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderId); // 商户订单号（唯一，不可重复）
            bizContent.put("total_amount", payAmount.toString()); // 订单金额（字符串，如"0.01"，不能是0）
            bizContent.put("subject", productName); // 订单标题（长度限制，简洁描述）
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY"); // 电脑网站支付固定产品码
            // 可选参数：超时时间（默认2h，格式：1d/2h/30m）
            bizContent.put("timeout_express", "2h");
            request.setBizContent(bizContent.toString());

            // 3. 调用支付宝接口，获取支付表单
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                // 返回的form表单：前端直接渲染（<form>标签），自动提交到支付宝支付页
                return response.getBody();
            } else {
                throw new RuntimeException("创建支付宝订单失败：" + response.getMsg() + "，" + response.getSubMsg());
            }
        } catch (Exception e) {
            throw new RuntimeException("支付宝订单创建异常", e);
        }
    }


    /**
     * {
     *   "alipay_trade_query_response": {
     *     "code": "10000",
     *     "msg": "Success",
     *     "out_trade_no": "20251215001",
     *     "trade_no": "2025121522001468801400000000",
     *     "trade_status": "SUCCESS",
     *     "gmt_payment": "2025-12-15 23:20:10", // 支付时间
     *     "total_amount": "0.01"
     *   },
     *   "sign": "xxx"
     * }
     * 查询支付宝订单支付状态
     * @param orderId 商户订单号（out_trade_no）
     * @return true=支付成功，false=未支付/关闭/失败
     */
    public AlipayOrderQueryResult queryOrderPayStatus(String orderId) {
        try {
            // 1. 创建查询请求对象
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderId); // 商户订单号（也可传trade_no：支付宝交易号）
            request.setBizContent(bizContent.toString());

            // 2. 调用查询接口
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                throw new RuntimeException("查询支付宝订单失败：" + response.getMsg());
            }
            String responseBody  = response.getBody();
            JSONObject responseJson = JSONObject.parseObject(responseBody);
            JSONObject queryResponse = responseJson.getJSONObject("alipay_trade_query_response");
            // 提取核心字段
            String tradeStatus = queryResponse.getString("trade_status"); // 订单状态
            String gmtPayment = queryResponse.getString("gmt_payment"); // 支付时间（关键）
            // 支付成功的状态值：SUCCESS（其他状态：WAIT_BUYER_PAY-待支付，CLOSED-关闭，TRADE_FINISHED-完结）
            return new AlipayOrderQueryResult("SUCCESS".equals(tradeStatus), gmtPayment);


        } catch (Exception e) {
            throw new RuntimeException("支付宝订单查询异常", e);
        }
    }

    /**
     * 主动关闭支付宝订单（适用于超时未支付的订单）
     * @param orderId 商户订单号（out_trade_no）
     * @return true=关单成功，false=关单失败
     */
    public boolean closeAlipayOrder(String orderId) {
        try {
            // 1. 创建关单请求对象
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderId); // 商户订单号
            // 可选：支付宝交易号（trade_no），优先级高于out_trade_no
            // bizContent.put("trade_no", "支付宝交易号");
            request.setBizContent(bizContent.toString());

            // 2. 调用支付宝关单接口
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                // 关单接口调用失败（如订单已支付/已关闭/不存在）
                throw new RuntimeException("支付宝关单接口调用失败：" + response.getMsg() + "，子错误：" + response.getSubMsg());
            }

            // 3. 解析关单结果（核心状态判断）
            String responseBizContent = response.getBody();
            JSONObject bizContentJson = JSONObject.parseObject(responseBizContent)
                    .getJSONObject("alipay_trade_close_response");
            String tradeStatus = bizContentJson.getString("trade_status"); // 获取订单状态
            String closeStatus = response.getCode(); // 接口返回码：10000=成功
            // 关单成功判断：接口调用成功 + 订单状态为TRADE_CLOSED
            return "10000".equals(closeStatus) && "TRADE_CLOSED".equals(tradeStatus);

            // 关单成功的场景：
            // - code=10000 且 trade_status=TRADE_CLOSED（订单未支付，关闭成功）
            // - 若订单已支付，会返回 TRADE_SUCCESS，此时关单失败
        } catch (Exception e) {
            throw new RuntimeException("支付宝关单异常，订单号：" + orderId, e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class AlipayOrderQueryResult {
        // 是否支付成功
        private boolean paySuccess;
        // 支付时间（未支付则为null）
        private String payTime;
    }
}
