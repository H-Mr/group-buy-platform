package cn.hjw.dev.platform.api;


import cn.hjw.dev.platform.api.response.Response;

public interface IAuthService {

    Response<String> weixinQrCodeTicket();

    Response<String> checkLogin(String ticket);

}
