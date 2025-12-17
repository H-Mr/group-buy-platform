package cn.hjw.dev.platform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeixinQrCodeResponseDTO {
    /**
     * 微信生成的二维码 Ticket (前端轮询鉴权时需要传回给后端)
     */
    private String ticket;

    /**
     * 二维码图片的 Base64 字符串 (前端用于展示)
     */
    private String tokenImage;
}
