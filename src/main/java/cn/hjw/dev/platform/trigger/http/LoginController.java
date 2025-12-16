package cn.hjw.dev.platform.trigger.http;

import cn.hjw.dev.platform.api.IAuthService;
import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.domain.auth.service.ILoginService;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/login/")
public class LoginController implements IAuthService {

    @Resource
    private ILoginService loginService;


    /**
     * http://xfg-studio.natapp1.cc/api/v1/login/weixin_qrcode_ticket
     * @return
     */
    @RequestMapping(value = "weixin_qrcode_ticket", method = RequestMethod.GET)
    @Override
    public Response<String> weixinQrCodeTicket() {
        try {
            String qrCodeTicket = loginService.createQrCodeTicket();
            log.info("生成微信扫码登录 ticket:{}", qrCodeTicket);
            return Response.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(qrCodeTicket)
                    .build();
        } catch (Exception e) {
            log.error("生成微信扫码登录 ticket 失败", e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * http://xfg-studio.natapp1.cc/api/v1/login/check_login
     */
    @RequestMapping(value = "check_login", method = RequestMethod.GET)
    @Override
    public Response<String> checkLogin(String ticket) {
        try {
            String token = loginService.checkLogin(ticket);
            // if openidToken is not blank, login successful。构造jwt返回
            log.info("扫码检测登录结果 ticket:{} token:{}", ticket, token);
            if (StringUtils.isNotBlank(token)) {
                return Response.<String>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(token)
                        .build();
            } else {

                return Response.<String>builder()
                        .code(ResponseCode.NO_LOGIN.getCode())
                        .info(ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }
        } catch (Exception e) {
            log.error("扫码检测登录结果失败 ticket:{}", ticket, e);
            return Response.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

}
