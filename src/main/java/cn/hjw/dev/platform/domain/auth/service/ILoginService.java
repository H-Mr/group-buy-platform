package cn.hjw.dev.platform.domain.auth.service;

import cn.hjw.dev.platform.api.dto.AuthTokenResponseDTO;

import java.io.IOException;

public interface ILoginService {

    /**
     * 检查登录状态
     * @param ticket
     * @return 返回生成的token
     */
    AuthTokenResponseDTO checkLogin(String ticket) throws IOException;

    /**
     * 保存登录状态,微信回调事件触发
     * @param ticket
     * @param openid
     * @throws IOException
     */
    void saveLoginState(String ticket, String openid) throws IOException;


    /**
     * 生成登录二维码图片
     * @return
     * @throws Exception
     */
    String generateLoginQrCodeImage() throws Exception;

    AuthTokenResponseDTO refreshAccessToken(String refreshToken);
}
