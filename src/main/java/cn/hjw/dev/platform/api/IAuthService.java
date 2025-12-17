package cn.hjw.dev.platform.api;


import cn.hjw.dev.platform.api.dto.AuthTokenResponseDTO;
import cn.hjw.dev.platform.api.response.Response;

import java.io.IOException;

public interface IAuthService {

    Response<String> weixinQrCodeTicket();

    Response<AuthTokenResponseDTO> checkLogin(String ticket) throws IOException;

}
