package cn.hjw.dev.platform.trigger.listener;

import cn.hjw.dev.platform.domain.auth.event.WeixinLoginSuccessEventTypeType;
import cn.hjw.dev.platform.infrastructure.gateway.IWeixinApiGateway;
import cn.hjw.dev.platform.infrastructure.gateway.dto.WeixinTemplateMessageDTO;
import cn.hjw.dev.platform.infrastructure.gateway.dto.WeixinTokenResponseDTO;
import cn.hjw.dev.platform.infrastructure.redis.IRedisService;
import cn.hjw.dev.platform.types.event.BaseEventType;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WeChatMsgListener {

    @Resource(name = "sys-event-bus")
    private EventBus eventBus;

    @Resource
    private IWeixinApiGateway weixinApiGateway; // 直接注入 API 服务

    @Resource
    private IRedisService redisService;

    @Value("${weixin.config.app-id}")
    private String appid;
    @Value("${weixin.config.app-secret}")
    private String appSecret;
    @Value("${weixin.config.template_id}")
    private String template_id;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onLoginSuccess(BaseEventType.Message<WeixinLoginSuccessEventTypeType.WeiXinLoginSuccess> event) {
        Object payload = event.getPayload();
        if (!(payload instanceof WeixinLoginSuccessEventTypeType.WeiXinLoginSuccess)) {
            return;
        }
        WeixinLoginSuccessEventTypeType.WeiXinLoginSuccess loginInfo = (WeixinLoginSuccessEventTypeType.WeiXinLoginSuccess) payload;
        String openid = loginInfo.getOpenid();
        log.info("监听到微信登录成功事件，准备发送模板消息: {}", openid);
        try {
            sendLoginTemplate(openid);
        } catch (Exception e) {
            log.error("发送微信模板消息失败", e);
        }
    }

    /**
     * 发送登录模板消息
     * @param openid
     * @throws IOException
     */
    private void sendLoginTemplate(String openid) throws IOException {
        // 1. 获取 accessToken 【实际业务场景，按需处理下异常】
        String accessToken = redisService.getValue(IWeixinApiGateway.WEIXIN_ACCESS_TOKEN);
        if (null == accessToken){
            // 重新请求获取 accessToken
            Call<WeixinTokenResponseDTO> call = weixinApiGateway.getToken("client_credential", appid, appSecret);
            WeixinTokenResponseDTO weixinTokenRes = call.execute().body();
            assert weixinTokenRes != null;
            accessToken = weixinTokenRes.getAccess_token();
            // 缓存 accessToken，提前180秒过期，避免临界点问题
            redisService.setValue("weixin_access_token_" + appid, accessToken, (weixinTokenRes.getExpires_in()*1000L) - 180000L);
        }

        // 2. 发送模板消息
        Map<String, Map<String, String>> data = new HashMap<>();
        WeixinTemplateMessageDTO.put(data, WeixinTemplateMessageDTO.TemplateKey.USER, openid);

        WeixinTemplateMessageDTO templateMessageDTO = new WeixinTemplateMessageDTO(openid, template_id);
        templateMessageDTO.setUrl("www.baidu.com");
        templateMessageDTO.setData(data);

        Call<Void> call = weixinApiGateway.sendMessage(accessToken, templateMessageDTO);
        call.execute();
    }

}