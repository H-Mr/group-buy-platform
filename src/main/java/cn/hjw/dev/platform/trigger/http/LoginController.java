package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.api.IAuthService;
import cn.hjw.dev.platform.api.dto.AuthTokenResponseDTO;
import cn.hjw.dev.platform.api.dto.WeixinQrCodeResponseDTO;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.domain.auth.service.ILoginService;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 登录控制器
 * 负责处理登录相关的HTTP请求
 * https://group-buy-plataform.apifox.cn
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/login/")
public class LoginController implements IAuthService {

    @Resource
    private ILoginService loginService;


    /**
     * 获取登录二维码
     * @return
     */
    @RequestMapping(value = "weixin_qrcode", method = RequestMethod.GET)
    @Override
    public Response<WeixinQrCodeResponseDTO> weixinQrCodeTicket() throws Exception {

        WeixinQrCodeResponseDTO weixinQrCodeResponseDTO = loginService.generateLoginQrCodeImage();
        if (ObjectUtils.isEmpty(weixinQrCodeResponseDTO)) {
            log.error("获取微信登录二维码失败");
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "获取微信登录二维码失败");
        }
        return Response.<WeixinQrCodeResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(weixinQrCodeResponseDTO)
                .build();
    }

    /**
     * 轮询扫码结果
     * @param ticket 登录票据
     */
    @RequestMapping(value = "check_login", method = RequestMethod.GET)
    @Override
    public Response<AuthTokenResponseDTO> checkLogin(String ticket) throws IOException {

            AuthTokenResponseDTO tokenPair = loginService.checkLogin(ticket);
            log.info("扫码检测登录结果 ticket:{} res:{}", ticket, tokenPair);
            if (ObjectUtils.isNotEmpty(tokenPair)) {
                return Response.<AuthTokenResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(tokenPair)
                        .build();
            }
            throw  new AppException(ResponseCode.NO_LOGIN.getCode(), "未扫码登录");
    }

    /**
     * 刷新 Token 接口
     * @param refreshToken 刷新令牌
     * 前端拦截器发现 401 后，携带 refreshToken 调用此接口
     */
    @GetMapping("refresh_token")
    public Response<AuthTokenResponseDTO> refreshToken(@RequestParam String refreshToken) {

            AuthTokenResponseDTO tokenInfo = loginService.refreshAccessToken(refreshToken);
            return Response.<AuthTokenResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(tokenInfo)
                    .build();
    }
}
