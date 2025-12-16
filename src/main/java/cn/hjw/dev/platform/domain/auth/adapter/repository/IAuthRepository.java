package cn.hjw.dev.platform.domain.auth.adapter.repository;

public interface IAuthRepository {

    /**
     * 为授权的二维码生成 token
     * @param ticket
     */
    String generateToken(String ticket);

    /**
     * 获取已登录用户的 token
     * @param userId 根据用户ID获取token
     * @return
     */
    String getToken(String userId);
    /**
     * 根据 OpenID 查询用户ID
     * @param openid
     * @return
     */
    String queryUserIdByOpenId(String openid);

    /**
     * 注册用户
     * @param openid
     * @param userId
     */
    void registerUser(String openid, String userId);



}
