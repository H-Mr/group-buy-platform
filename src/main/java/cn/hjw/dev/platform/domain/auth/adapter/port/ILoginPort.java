package cn.hjw.dev.platform.domain.auth.adapter.port;

import java.io.IOException;

public interface ILoginPort {

    /**
     * 创建登录二维码票据
     * @return
     * @throws IOException
     */
    String createQrCodeTicket() throws IOException;

    /**
     * 保存登录状态
     * @param ticket
     * @param openid
     * @throws IOException
     */
    void saveLoginState(String ticket, String openid) throws IOException;

    /**
     * 检查登录状态
     * @param ticket
     * @return 是否登录
     * @throws IOException
     */
    boolean checkLoginState(String ticket) throws IOException;


    /**
     * 生成登录二维码图片
     * @return
     * @throws Exception
     */
    String generateLoginQrCodeImage(String ticket) throws Exception;



}
