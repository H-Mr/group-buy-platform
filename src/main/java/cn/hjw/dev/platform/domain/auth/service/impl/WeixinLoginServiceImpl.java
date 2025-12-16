package cn.hjw.dev.platform.domain.auth.service.impl;

import cn.hjw.dev.platform.domain.auth.adapter.port.ILoginPort;
import cn.hjw.dev.platform.domain.auth.adapter.repository.IAuthRepository;
import cn.hjw.dev.platform.domain.auth.service.ILoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Service
public class WeixinLoginServiceImpl implements ILoginService {

    @Resource
    private ILoginPort loginPort; // 获取微信服务

    @Resource
    private IAuthRepository authRepository; // 认证仓储

    @Override
    public String checkLogin(String ticket) throws IOException {
        boolean isScan = loginPort.checkLoginState(ticket);// 根据 ticket 获取登录状态
        String token = null;
        if (isScan) {
            // 已扫码登录，生成 token 存入redis
            token = authRepository.generateToken(ticket);
        }
        return token;
    }

    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {
        loginPort.saveLoginState(ticket, openid);
    }


    @Override
    public String generateLoginQrCodeImage() throws Exception {
        String qrCodeTicket = loginPort.createQrCodeTicket();
        return loginPort.generateLoginQrCodeImage(qrCodeTicket);
    }
}

