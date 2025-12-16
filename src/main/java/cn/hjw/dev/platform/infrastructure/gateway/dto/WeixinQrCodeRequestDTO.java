package cn.hjw.dev.platform.infrastructure.gateway.dto;

import lombok.*;

/**
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 * @description 获取微信登录二维码请求对象
 * @create 2024-02-25 09:32
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeixinQrCodeRequestDTO {

    private int expire_seconds; // 该二维码有效时间，以秒为单位。 最大不超过2592000（即30天），此字段如果不填，则默认有效期为30秒。
    private String action_name; // 二维码类型，QR_SCENE为临时的整型参数值，QR_STR_SCENE为临时的字符串参数值，QR_LIMIT_SCENE为永久的整型参数值，QR_LIMIT_STR_SCENE为永久的字符串参数值
    private ActionInfo action_info; // 二维码详细信息

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActionInfo {
        Scene scene;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Scene {
            int scene_id; // 场景值ID，临时二维码时为32位非0整型，永久二维码时最大值为100000
            String scene_str; // 场景值ID（字符串形式的ID），字符串类型，长度限制为1到64
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum ActionNameTypeVO {
        QR_SCENE("QR_SCENE", "临时的整型参数值"),
        QR_STR_SCENE("QR_STR_SCENE", "临时的字符串参数值"),
        QR_LIMIT_SCENE("QR_LIMIT_SCENE", "永久的整型参数值"),
        QR_LIMIT_STR_SCENE("QR_LIMIT_STR_SCENE", "永久的字符串参数值");

        private String code;
        private String info;
    }

}
