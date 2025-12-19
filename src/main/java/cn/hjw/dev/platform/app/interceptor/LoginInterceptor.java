package cn.hjw.dev.platform.app.interceptor;

import cn.hjw.dev.platform.infrastructure.dcc.DynamicConfigCenter;
import cn.hjw.dev.platform.types.common.Constants;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import cn.hjw.dev.platform.types.utils.JwtUtils;
import cn.hjw.dev.platform.types.utils.UserContext;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    // 注入 DCC，用于动态获取体验 Token 配置
    private final DynamicConfigCenter dynamicConfigCenter;

    public LoginInterceptor(JwtUtils jwtUtils, DynamicConfigCenter dynamicConfigCenter) {
        this.jwtUtils = jwtUtils;
        this.dynamicConfigCenter = dynamicConfigCenter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 Token，通常放在 Header 的 Authorization 字段
        // 前端传参 header: { "Authorization": "Bearer token_string" }
        String token = request.getHeader("Authorization");

        // 兼容处理：如果 header 没拿不到，也可以尝试从参数获取（可选）
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }

        if (StringUtils.isBlank(token)) {
            // 抛出异常，由全局异常处理器捕获返回 NO_LOGIN 错误码
            throw new AppException(ResponseCode.NO_LOGIN.getCode(), "未提供登录Token，格式为 Bearer xxxx");
        }
        // 逻辑：如果 Token 匹配 DCC 管理密钥，且请求路径是 DCC 相关接口，则直接放行
        String dccSecret = dynamicConfigCenter.getDccAdminSecret();
        if (StringUtils.isNotBlank(dccSecret) && dccSecret.equals(token)) {
            String requestURI = request.getRequestURI();
            // 严格限制：只有访问 /api/v1/gbm/dcc/ 下的接口才允许通过
            if (requestURI.contains("/api/v1/gbm/dcc/")) {
                log.info("DCC管理密钥访问，路径: {}", requestURI);
                return true; // 直接放行，跳过后续所有校验
            } else {
                // 如果拿着 DCC 密钥去访问下单接口，视为非法，拒绝或让其走后续 JWT 校验（自然会失败）
                log.warn("警告：尝试使用 DCC 密钥访问非 DCC 接口: {}", requestURI);
                // 这里选择不 return true，让代码继续往下走。
                // 因为这个 token 不是有效的 JWT，下面解析时会报错从而拦截，达到"只能访问DCC"的目的。
            }
        }

        // 1. 判断开关是否开启
        if (dynamicConfigCenter.isDemoTokenOpen()) {
            // 2. 匹配 Token 字符串
            if (token.equals(dynamicConfigCenter.getDemoTokenSecret())) {
                String demoUserId = dynamicConfigCenter.getDemoUserId();
                log.info("演示模式：使用体验Token登录，用户ID: {}", demoUserId);
                // 3. 直接设置上下文，跳过 JWT 解析
                UserContext.setUserId(demoUserId);
                return true;
            }
        }
        // ================= 核心修改结束 =================

        try {
            Claims claims = jwtUtils.parseToken(token);
            String userId = (String) claims.get("userId");
            if (StringUtils.isBlank(userId)) {
                throw new AppException(ResponseCode.NO_LOGIN.getCode(), "Token无效：缺少用户信息");
            }
            UserContext.setUserId(userId);
            return true;
        } catch (Exception e) {
            // 注意：前端遇到此报错(code: 401/NO_LOGIN)，应该触发使用 refreshToken 换取新 token 的流程
            log.warn("拦截器鉴权失败，Token: {}", token);
            throw new AppException(ResponseCode.NO_LOGIN.getCode(), "登录已过期");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 4. 请求结束后清除 ThreadLocal，防止内存泄漏（必做）
        UserContext.remove();
    }
}