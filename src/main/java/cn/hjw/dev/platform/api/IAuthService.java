package cn.hjw.dev.platform.api;


import cn.hjw.dev.platform.api.dto.AuthTokenResponseDTO;
import cn.hjw.dev.platform.api.dto.WeixinQrCodeResponseDTO;
import cn.hjw.dev.platform.api.response.Response;

import java.io.IOException;

public interface IAuthService {

    Response<WeixinQrCodeResponseDTO> weixinQrCodeTicket() throws Exception;

    Response<AuthTokenResponseDTO> checkLogin(String ticket) throws IOException;

}
