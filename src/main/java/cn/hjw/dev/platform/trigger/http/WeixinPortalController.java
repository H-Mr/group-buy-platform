package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.domain.auth.service.ILoginService;
import cn.hjw.dev.platform.types.sdk.weixin.MessageTextEntity;
import cn.hjw.dev.platform.types.sdk.weixin.SignatureUtil;
import cn.hjw.dev.platform.types.sdk.weixin.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 微信门户控制器
 * 用于服务器与微信服务器进行交互
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/weixin/portal/")
public class WeixinPortalController {

    @Value("${weixin.config.originalid}")
    private String originalid;
    @Value("${weixin.config.token}")
    private String token;

    @Resource
    private ILoginService loginService;


    // produces会显式设置 HTTP 响应头中的Content-Type，告诉客户端服务器返回的内容格式和字符编码。text/plain：表示响应体是纯文本类型；
    // 客户端发起请求时，会通过Accept请求头告诉服务器 “我能接收的内容类型”（比如Accept: application/json
    // Spring 会根据produces的值筛选匹配的控制器方法：只有客户端的Accept头包含produces指定的类型，
    // 该方法才会被选中处理请求；若没有匹配的方法，会返回406 Not Acceptable（无法接受的）错误。
    // text/plain;charset=utf-8	    纯文本 + UTF-8 编码	    返回简单字符串、文本内容
    // application/json;charset=utf-8	JSON 格式 + UTF-8 编码	RESTful API 返回 JSON 数据（最常用）
    // application/xml;charset=utf-8	XML 格式 + UTF-8 编码	返回 XML 格式数据
    // text/html;charset=utf-8	    HTML 格式 + UTF-8 编码	返回网页内容
    // produces：约束响应体的类型，匹配客户端的Accept请求头；
    // consumes：约束请求体的类型，匹配客户端的Content-Type请求头。
    /**
     * 微信公众号接入验证
     * 微信服务器向开发者配置的 URL 发起 GET 请求，验证该 URL 是否为开发者所有，是公众号对接的 “准入验证”。
     * 只有验签通过，后续的消息推送（POST）才会生效。
     * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param echostr 随机字符串（验签成功后需原样返回，微信服务器以此确认验证通过）
     * @return echostr参数内容
     *
     * 服务器侧：
     * 微信和开发者约定一个token（公众号后台配置），验签时按以下步骤：
     * 1. 将token、timestamp、nonce三个参数进行字典序排序
     * 2. 将三个参数字符串拼接成一个字符串进行sha1加密
     * 3. 开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
     * 对比加密结果与signature，一致则验签成功。
     * -------------------------------------------------
     * 微信侧：
     * 若开发者服务器返回的echostr与微信发送的一致 → 验证通过，公众号后台配置生效；
     * 若返回null/ 空 / 错误值 → 验证失败，配置无法保存。
     */
    @GetMapping(value = "receive", produces = "text/plain;charset=utf-8")
    public String validate(@RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            log.info("微信公众号验签信息开始 [{}, {}, {}, {}]", signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }
            boolean check = SignatureUtil.check(token, timestamp, nonce,signature);
            log.info("微信公众号验签信息完成 check：{}", check);
            if (!check) {
                return null;
            }
            return echostr;
        } catch (Exception e) {
            log.error("微信公众号验签信息失败 [{}, {}, {}, {}]", signature, timestamp, nonce, echostr, e);
            return null;
        }
    }

    /*
     * 验证通过后，当用户向公众号发送消息（文字、图片、语音等），
     * 微信服务器会将消息封装为 XML 格式，通过 POST 请求推送到该接口，开发者处理后返回 XML 格式的响应（如回复用户 “你好，XXX”）。
     * value = "receive"：与 GET 请求共用同一个 URL（微信规范，验证和消息推送都用同一个 URL，通过请求方法区分）；
     * */
    /**
     * 接收微信公众号消息
     * 微信服务器将用户发送的消息转发给开发者服务器，开发者处理后响应特定格式的数据包给微信服务器，
     * 微信服务器再将响应内容转发给用户。
     * @param requestBody 消息体（XML格式）
     * @param signature 微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param openid 用户标识
     * @param encType 加密类型（明文：raw；加密：aes）
     * @param msgSignature 消息体签名（加密消息才有）
     * @return 响应给微信服务器的消息体（XML格式）
     */
    @PostMapping(value = "receive", produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody, // 消息体,xml格式,微信服务器发送过来的数据
                       @RequestParam("signature") String signature, // 微信加密签名
                       @RequestParam("timestamp") String timestamp, // 时间戳
                       @RequestParam("nonce") String nonce, // 随机数
                       @RequestParam("openid") String openid, // 发送用户标识
                       @RequestParam(name = "encrypt_type", required = false) String encType, // 消息加密类型（如aes，仅公众号开启消息加密时才传）
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) { // 加密消息的签名（仅开启加密时传，用于验证加密消息的有效性）
        try {
            log.info("接收微信公众号信息请求{},消息体为：{}", openid, requestBody);
            // 消息转换
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);

            if ("event".equals(message.getMsgType()) && "SCAN".equals(message.getEvent())) {
                log.info("用户扫码关注， ticket：{}， openid：{}", message.getTicket(), openid);
                // 用户已扫码关注，保存登录状态
                loginService.saveLoginState(message.getTicket(), openid);
                return null;
            }

            return buildMessageTextEntity(openid, "你好，" + message.getContent()); // 微信接收也是接收xml格式
        } catch (Exception e) {
            log.error("接收微信公众号信息请求{}失败 {}", openid, requestBody, e);
            return "";
        }
    }

    private String buildMessageTextEntity(String openid, String content) {
        MessageTextEntity res = new MessageTextEntity();
        // 公众号分配的ID
        res.setFromUserName(originalid);
        res.setToUserName(openid);
        res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
        res.setMsgType("text");
        res.setContent(content);
        return XmlUtil.beanToXml(res);
    }

}

