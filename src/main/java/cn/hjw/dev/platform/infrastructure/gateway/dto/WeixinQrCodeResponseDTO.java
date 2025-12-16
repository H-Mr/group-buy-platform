package cn.hjw.dev.platform.infrastructure.gateway.dto;

import lombok.Data;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 获取微信登录二维码响应对象
 * @create 2024-02-25 09:36
 */
@Data
public class WeixinQrCodeResponseDTO {

    private String ticket; // 获取二维码票据，凭借此票据可以在有效时间内换取二维码。
    private Long expire_seconds; // 该二维码有效时间，以秒为单位。 最大不超过2592000（即30天）。
    private String url; // 二维码图片解析后的地址，开发者可根据该地址自行生成需要的二维码图片

}
