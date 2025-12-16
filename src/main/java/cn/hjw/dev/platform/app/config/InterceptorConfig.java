package cn.hjw.dev.platform.app.config;

import cn.hjw.dev.platform.app.interceptor.LoginInterceptor;
import cn.hjw.dev.platform.types.utils.JwtUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 LoginInterceptor
        registry.addInterceptor(new LoginInterceptor(jwtUtils))
                // 指定拦截的路径
                .addPathPatterns("/api/v1/**")
                // 排除登录接口
                .excludePathPatterns("/api/v1/login/**")
                // 排除第三方回调接口
                .excludePathPatterns("/api/v1/weixin/portal/**")
                .excludePathPatterns("/api/v1/alipay/alipay_notify_url")
                // 公共查询接口开放（浏览SKU、活动详情不需要登录）
                .excludePathPatterns("/api/v1/query/**");

        ;
    }
}
