package cn.hjw.dev.platform.app.interceptor;

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

    // 构造注入，方便从 Config 传入
    public LoginInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取 Token，通常放在 Header 的 Authorization 字段，或者自定义字段如 "token"
        // 这里假设前端传参 header: { "Authorization": "token_string" }
        String token = request.getHeader("Authorization");

        // 兼容处理：如果 header 没拿不到，也可以尝试从参数获取（可选）
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }

        if (StringUtils.isBlank(token)) {
            // 抛出异常，由全局异常处理器捕获返回 NO_LOGIN 错误码
            throw new AppException(ResponseCode.NO_LOGIN.getCode(), ResponseCode.NO_LOGIN.getInfo());
        }

        try {
            // 2. 解析 Token
            Claims claims = jwtUtils.parseToken(token);

            String userId = (String) claims.get("userId");

            if (StringUtils.isBlank(userId)) {
                throw new AppException(ResponseCode.NO_LOGIN.getCode(), "Token无效：缺少用户信息");
            }

            // 3. 将 userId 放入上下文，供后续 Controller 使用
            UserContext.setUserId(userId);

            return true; // 放行

        } catch (Exception e) {
            log.warn("拦截器鉴权失败，Token: {}", token, e);
            throw new AppException(ResponseCode.NO_LOGIN.getCode(), "登录已过期，请重新扫码登录");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 4. 请求结束后清除 ThreadLocal，防止内存泄漏（必做）
        UserContext.remove();
    }
}