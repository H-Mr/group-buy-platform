package cn.hjw.dev.platform.domain.auth.service;

import java.io.IOException;

public interface ILoginService {

    /**
     * 创建登录二维码票据
     * @return
     * @throws Exception
     */
    String createQrCodeTicket() throws Exception;

    /**
     * 检查登录状态
     * @param ticket
     * @return 返回生成的token
     */
    String checkLogin(String ticket) throws IOException;

    /**
     * 保存登录状态
     * @param ticket
     * @param openid
     * @throws IOException
     */
    void saveLoginState(String ticket, String openid) throws IOException;

    /**
     * 根据 ticket 获取 token
     * @param ticket
     * @return
     */
    String getToken(String ticket);
}
