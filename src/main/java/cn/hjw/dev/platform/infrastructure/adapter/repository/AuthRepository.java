package cn.hjw.dev.platform.infrastructure.adapter.repository;

import cn.hjw.dev.platform.domain.auth.adapter.repository.IAuthRepository;
import cn.hjw.dev.platform.infrastructure.dao.IUserDao;
import cn.hjw.dev.platform.infrastructure.dao.po.User;
import cn.hjw.dev.platform.infrastructure.gateway.IWeixinApiGateway;
import cn.hjw.dev.platform.infrastructure.redis.IRedisService;
import cn.hjw.dev.platform.types.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Slf4j
@Repository
public class AuthRepository  implements IAuthRepository {

    @Resource
    IUserDao userDao;

    @Resource
    private IRedisService redisService; // Redis 服务

    @Resource
    private JwtUtils jwtUtils; // Jwt 工具类 生成登录用户的token

    @Override
    public String generateToken(String ticket) {
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
        // 使用Jwt生成token,并存储到redis中
        // 3. 生成 JWT (包含 userId)
        String token = jwtUtils.generateToken(user.getUserId(), openid);
        // 4.
        // 4. 存储 token 到 Redis，设置过期时间，Key 必须方便后续校验
        // 场景：前端传 Header: Authorization -> Token
        // 后端解析 Token -> 拿 userId -> 查 Redis: "token:user:{userId}" 是否等于当前 Token
        // 这样可以实现单点登录（新 Token 覆盖旧 Token）
        String redisKey = "token:user:" + user.getUserId();
        redisService.setValue(redisKey, token, jwtUtils.getExpire()*1000L);
        return token; // 返回 token
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
}
