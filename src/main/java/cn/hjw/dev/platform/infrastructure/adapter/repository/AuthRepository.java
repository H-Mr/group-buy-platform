package cn.hjw.dev.platform.infrastructure.adapter.repository;

import cn.hjw.dev.platform.api.dto.AuthTokenResponseDTO;
import cn.hjw.dev.platform.domain.auth.adapter.repository.IAuthRepository;
import cn.hjw.dev.platform.infrastructure.dao.IUserDao;
import cn.hjw.dev.platform.infrastructure.dao.po.User;
import cn.hjw.dev.platform.infrastructure.gateway.IWeixinApiGateway;
import cn.hjw.dev.platform.infrastructure.redis.IRedisService;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.platform.types.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.UUID;

@Slf4j
@Repository
public class AuthRepository  implements IAuthRepository {

    @Resource
    IUserDao userDao;

    @Resource
    private IRedisService redisService; // Redis 服务

    @Resource
    private JwtUtils jwtUtils; // Jwt 工具类 生成登录用户的token

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final long REFRESH_EXPIRE_DAYS = 7;

    @Override
    public AuthTokenResponseDTO generateToken(String ticket) {
        // 获取ticket对应的openid
        String openid = redisService.getValue(IWeixinApiGateway.WEIXIN_QRCODE_TICKET_PREFIX + ticket);
        User user = userDao.queryUserByOpenId(openid);
        if (ObjectUtils.isEmpty(user)) {
            // 注册用户
            String userId = RandomStringUtils.randomNumeric(64); // 生成64位随机用户ID
            this.registerUser(openid, userId);
            user = User.builder().openid(openid).userId(userId).build();
            log.info("新用户注册成功，userId：{}，openid：{}", userId, openid);
        }

        return createTokenPair(user.getUserId(),user.getOpenid()); // 返回 token
    }

    @Override
    public String getToken(String userId) {
        return redisService.getValue("token:user:"+userId);
    }

    @Override
    public String queryUserIdByOpenId(String openid) {
        User user = userDao.queryUserByOpenId(openid);
        if (user == null) return null;
        return user.getUserId();
    }

    @Override
    public void registerUser(String openid, String userId) {
        User user = User.builder()
                .userId(userId)
                .openid(openid)
                .build();
        try {
            userDao.insert(user);
        } catch (DuplicateKeyException e) {
            // 忽略并发注册冲突
        }
    }

    @Override
    public AuthTokenResponseDTO refreshAccessToken(String refreshToken) {
        String redisKey = REFRESH_TOKEN_PREFIX + refreshToken;
        // 1. 校验 Refresh Token 是否存在
        String userId = redisService.getValue(redisKey);
        if (StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.NO_LOGIN.getCode(), "Refresh Token 已过期或无效，请重新登录");
        }
        // 2. 核心：只要还能刷，就重置 Refresh Token 的过期时间 (7天)
        // 这样只要用户 7 天内有一步操作触发了刷新，他就永远不用重新扫码
        redisService.setValue(redisKey, userId, REFRESH_EXPIRE_DAYS * 24 * 60 * 60 *1000L);
        // 3. 生成新的 Access Token
        String newAccessToken = jwtUtils.generateToken(userId, "");

        return AuthTokenResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // RefreshToken 保持不变，但 Redis 中 TTL 已延长
                .expiresIn(jwtUtils.getExpire())
                .build();
    }

    private AuthTokenResponseDTO createTokenPair(String userId, String openid) {
        // 1. 生成 Access Token (JWT)
        String accessToken = jwtUtils.generateToken(userId, openid);

        // 2. 生成 Refresh Token (UUID)
        String refreshToken = UUID.randomUUID().toString().replace("-", "");

        // 3. 存储 Refresh Token 到 Redis，有效期 7 天
        redisService.setValue(REFRESH_TOKEN_PREFIX + refreshToken, userId, REFRESH_EXPIRE_DAYS * 24 * 60 * 60);

        return AuthTokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getExpire())
                .build();
    }
}
