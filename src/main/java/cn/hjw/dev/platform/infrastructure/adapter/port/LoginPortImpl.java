package cn.hjw.dev.platform.infrastructure.adapter.port;


import cn.hjw.dev.platform.domain.auth.adapter.port.ILoginPort;
import cn.hjw.dev.platform.domain.auth.event.WeixinLoginSuccessEventTypeType;
import cn.hjw.dev.platform.infrastructure.gateway.IWeixinApiGateway;
import cn.hjw.dev.platform.infrastructure.gateway.dto.WeixinQrCodeRequestDTO;
import cn.hjw.dev.platform.infrastructure.gateway.dto.WeixinQrCodeResponseDTO;
import cn.hjw.dev.platform.infrastructure.gateway.dto.WeixinTokenResponseDTO;
import cn.hjw.dev.platform.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Service
public class LoginPortImpl implements ILoginPort {

    @Value("${weixin.config.app-id}")
    private String appid;
    @Value("${weixin.config.app-secret}")
    private String appSecret;
    @Value("${weixin.config.template_id}")
    private String template_id;

    @Resource
    private IRedisService redisService; // Redis 服务

    @Resource
    private IWeixinApiGateway weixinApiGateway; // 发送微信请求

    @Resource
    private WeixinLoginSuccessEventTypeType weixinLoginSuccessEventTypeType;

    @Override
    public String createQrCodeTicket() throws IOException {
        // 1. 获取 accessToken
        String accessToken = redisService.getValue(IWeixinApiGateway.WEIXIN_ACCESS_TOKEN);
        if (null == accessToken) {
            // 重新请求获取 accessToken
            Call<WeixinTokenResponseDTO> call = weixinApiGateway.getToken("client_credential", appid, appSecret);
            WeixinTokenResponseDTO weixinTokenRes = call.execute().body();
            assert weixinTokenRes != null;
            accessToken = weixinTokenRes.getAccess_token();
            // 缓存 accessToken，提前180秒过期，避免临界点问题
            redisService.setValue(IWeixinApiGateway.WEIXIN_ACCESS_TOKEN, accessToken, (weixinTokenRes.getExpires_in()*1000L) - 180000L);
        }

        // 2. 构造请求，创建登录二维码 ticket
        WeixinQrCodeRequestDTO weixinQrCodeReq = WeixinQrCodeRequestDTO.builder()
                .action_name(WeixinQrCodeRequestDTO.ActionNameTypeVO.QR_SCENE.getCode()) // 临时二维码
                .action_info(WeixinQrCodeRequestDTO.ActionInfo.builder() // 场景值
                        .scene(WeixinQrCodeRequestDTO.ActionInfo.Scene.builder()
                                .scene_id(100601)
                                .build())
                        .build())
                .build();

        // 3. 请求获取 ticket
        Call<WeixinQrCodeResponseDTO> call = weixinApiGateway.createQrCode(accessToken, weixinQrCodeReq);
        WeixinQrCodeResponseDTO weixinQrCodeRes = call.execute().body();
        assert null != weixinQrCodeRes;
        return weixinQrCodeRes.getTicket();
    }

    /**
     * 保存每个ticket对应的openId
     * @param ticket
     * @param openid
     * @throws IOException
     */
    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {
        // 保存微信扫码登录成功状态到 Redis
        log.info("保存微信扫码登录状态 ticket：{}， openid：{}", ticket, openid);
        redisService.setValue(IWeixinApiGateway.WEIXIN_QRCODE_TICKET_PREFIX+ticket, openid, 5*60*1000L); // 5分钟有效期
        // 发送微信登录成功事件，将发送登录成功模板消息从主流程中解耦
        weixinLoginSuccessEventTypeType.publishWeiXinSuccessEvent(openid);
    }

    /**
     * 检查登录状态
     * @param ticket
     * @return 是否登录
     * @throws IOException
     */
    @Override
    public boolean checkLoginState(String ticket) throws IOException {
        return StringUtils.isNotBlank(redisService.getValue(IWeixinApiGateway.WEIXIN_QRCODE_TICKET_PREFIX+ticket));
    }


}
