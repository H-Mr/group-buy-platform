package cn.hjw.dev.platform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenResponseDTO implements Serializable {
    private String accessToken;  // 短效，用于请求
    private String refreshToken; // 长效，用于刷新
    private long expiresIn;      // accessToken 过期时间
}
