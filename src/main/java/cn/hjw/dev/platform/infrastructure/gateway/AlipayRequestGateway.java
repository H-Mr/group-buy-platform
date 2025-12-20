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

/*
Q1：调用 SDK 创建支付表单，支付宝那边不存当前订单吗？
回答：不存。
CreatePagePay (生成表单)：这只是本地字符串拼接操作，生成一段 HTML 代码。此时支付宝服务端完全不知道这笔订单的存在。

用户扫码/打开收银台：只有当用户真正扫码或在浏览器跳转到支付宝页面时，支付宝才会创建这笔交易（状态通常为 WAIT_BUYER_PAY）。

如果用户没扫码：你查单会报 ACQ.TRADE_NOT_EXIST（交易不存在）。

如果用户扫码了没付：你查单会报 WAIT_BUYER_PAY（等待买家付款）。

Q2：如果用户超过 5 分钟支付怎么办？不是错误关单了吗？
回答：这就是“支付与关单的竞态”问题。
我们设置 15 分钟（或 5 分钟）超时，但支付宝的二维码默认有效期可能是 2 小时。

必须以“关单”操作为准。如果用户在第 16 分钟支付，而你的定时任务刚好在第 16 分钟跑：

你的系统发起 alipay.trade.close。

如果关单成功：用户那边支付界面会提示“订单已失效/关闭”，无法支付。这是安全的。

如果关单失败（报错 ACQ.TRADE_STATUS_ERROR）：说明用户已经支付成功了。此时你不能把本地订单改成关闭，而应该改成支付成功。

Q3：我现在处理为什么错误？
回答：因为你把“关单失败”全部当成了异常抛出，而没有区分“因为已支付所以关单失败”的情况。
场景还原（Race Condition）：

02:25:00.851 (Query)：你的任务查单，此时用户可能刚扫码或者刚点支付，支付宝状态可能还是 WAIT_BUYER_PAY（或者你的查询逻辑判断未支付）。于是代码走到了 else 分支准备关单。

毫秒级的时间差：在这极短的时间内，用户的支付交易在支付宝侧完成了（变为 TRADE_SUCCESS）。

02:25:01.126 (Close)：你的代码发起关单。支付宝发现订单已支付，不允许关闭，于是报错 ACQ.TRADE_STATUS_ERROR（当前交易状态不支持此操作）。

你的代码崩溃：你捕获了异常并打印 Error，本地订单状态既没变成功，也没变关闭，卡住了。

2. 正确的支付宝对接全流程（最终一致性方案）
要在分布式环境中保证订单状态正确，必须遵循**“查单 -> 尝试关单 -> 根据关单结果修正状态”**的逻辑。

核心逻辑图解
查询 (Query)：先看支付宝状态。

如果是 SUCCESS -> 本地标为支付成功。

如果是 NOT_EXIST -> 本地标为关闭（因为还没去支付宝建单）。

如果是 WAIT_BUYER_PAY -> 进入关单流程。

关单 (Close)：这是最终判决。

关单成功 -> 本地标为关闭（用户确实没付，且以后也付不了了）。

关单失败（交易不存在） -> 本地标为关闭（等同于关单成功）。

关单失败（状态不允许/已支付） -> 本地标为支付成功（说明用户手快支付了，我们必须承认这笔交易）。


支付宝提供了一个专用字段 passback_params（公用回传参数），用于透传商户自定义数据。你发送请求时传给支付宝，支付宝在**异步通知（Notify）和同步跳转（Return）**时会原样返回给你。
* */

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
    public String createPagePayOrder(String orderId, BigDecimal payAmount, String productName,String source, String channel) {
        try {
            log.info("创建支付宝支付订单，orderId：{}，payAmount：{}，source：{}，channel：{}", orderId, payAmount, source, channel);
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
            // 1. 拼接透传参数，建议使用 JSON 格式或 Key-Value 格式
            // 格式示例：source=s01&channel=c01
            String customParams = String.format("source=%s&channel=%s", source, channel);
            // 2. 必须进行 URL Encode 编码 (这是支付宝强制要求的)
            String passbackParams = java.net.URLEncoder.encode(customParams, "UTF-8");
            // 3. 放入 bizContent
            bizContent.put("passback_params", passbackParams);

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
                // 特殊处理：如果报错是“交易不存在”，说明用户没扫码或没登录支付宝，视为“未支付”
                if ("ACQ.TRADE_NOT_EXIST".equals(response.getSubCode())) {
                    log.warn("支付宝订单查询：交易不存在（用户可能未扫码），orderId：{}", orderId);
                    // 返回 false 代表未支付，payTime 为 null
                    return new AlipayOrderQueryResult(false, null);
                }
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
     * @return true=关单成功(订单已取消); false=关单失败(订单已支付或已完成，不可关闭)
     */
    public boolean closeAlipayOrder(String orderId) {
        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderId);
            request.setBizContent(bizContent.toString());

            AlipayTradeCloseResponse response = alipayClient.execute(request);

            // 1. 接口调用成功 (code=10000)
            if (response.isSuccess()) {
                return true; // 正常关单
            }

            // 2. 接口调用失败，分析错误码
            String subCode = response.getSubCode();

            // 情况 A: 交易不存在 (ACQ.TRADE_NOT_EXIST)
            // 含义: 用户根本没扫码。视为关单成功。
            if ("ACQ.TRADE_NOT_EXIST".equals(subCode)) {
                log.info("关单：交易不存在，视为关闭成功。orderId: {}", orderId);
                return true;
            }

            // 情况 B: 交易状态不允许 (ACQ.TRADE_STATUS_ERROR)
            // 含义: 订单已支付(TRADE_SUCCESS) 或 已结束(TRADE_FINISHED)，无法关闭。
            // 动作: 视为关单失败（其实是支付成功了），返回 false
            if ("ACQ.TRADE_STATUS_ERROR".equals(subCode)) {
                log.warn("关单：交易已支付或已结束，无法关闭。orderId: {}", orderId);
                return false;
            }

            // 其他未知错误，抛出异常
            throw new RuntimeException("支付宝关单失败: " + response.getSubMsg());

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
