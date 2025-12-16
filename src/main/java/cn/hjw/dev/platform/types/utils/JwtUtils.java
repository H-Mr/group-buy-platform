package cn.hjw.dev.platform.types.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Getter
    @Value("${jwt.expire}")
    private long expire; // 单位：秒

    /**
     * 生成JWT Token
     */
    public String generateToken(String userId, String openid) {
        Date now = new Date();
        // expire 是秒，需要 * 1000 转毫秒
        Date expireDate = new Date(now.getTime() + expire * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("openid", openid);

        return Jwts.builder()
                .setClaims(claims) // 放入自定义数据
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret) // 必须确保 secret 长度足够
                .compact();
    }

    // 解析 Token 方法（后续鉴权用）
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

}
