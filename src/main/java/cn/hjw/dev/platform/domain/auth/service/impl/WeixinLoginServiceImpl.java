package cn.hjw.dev.platform.domain.auth.service.impl;

import cn.hjw.dev.platform.api.dto.AuthTokenResponseDTO;
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
    public AuthTokenResponseDTO checkLogin(String ticket) throws IOException {
        // 1. 检查微信登录状态
        boolean isScan = loginPort.checkLoginState(ticket);// 根据 ticket 获取登录状态
        if (!isScan) {
            return null; // 未扫码
        }
        return authRepository.generateToken(ticket);
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

    @Override
    public AuthTokenResponseDTO refreshAccessToken(String refreshToken) {
        return authRepository.refreshAccessToken(refreshToken);
    }
}

