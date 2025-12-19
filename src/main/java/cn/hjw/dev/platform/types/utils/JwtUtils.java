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
    private long expire; // 默认配置过期时间（单位：秒）

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
                // 使用了 SignatureAlgorithm.HS256。HS256 (HMAC-SHA256) 算法规范要求密钥长度至少为 256 位（32 字节）。
                // secret 长度：至少 32 个字符（建议 64 个字符以上），包含大小写字母、数字和特殊符号。
                .signWith(SignatureAlgorithm.HS256, secret) // 必须确保 secret 长度足够
                .compact();
    }

    /**
     * [核心方法] 生成JWT Token (支持动态指定过期时间)
     *
     * @param userId    用户ID
     * @param openid    微信OpenId
     * @param ttlMillis 过期时间（毫秒）
     * @return Token字符串
     */
    public String generateToken(String userId, String openid, long ttlMillis) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // 使用传入的 ttlMillis 计算过期时间
        Date expireDate = new Date(nowMillis + ttlMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("openid", openid);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 解析 Token 方法（后续鉴权用）
    public Claims parseToken(String tokenStr) {
        String token = tokenStr.replace("Bearer ", "");
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

}
