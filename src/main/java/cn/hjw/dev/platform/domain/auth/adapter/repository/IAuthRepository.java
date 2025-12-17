package cn.hjw.dev.platform.domain.auth.adapter.repository;

import cn.hjw.dev.platform.api.dto.AuthTokenResponseDTO;

public interface IAuthRepository {

    /**
     * 为授权的二维码生成 token
     * @param ticket
     */
    AuthTokenResponseDTO generateToken(String ticket);

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

    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌响应DTO
     */
    AuthTokenResponseDTO refreshAccessToken(String refreshToken);



}
