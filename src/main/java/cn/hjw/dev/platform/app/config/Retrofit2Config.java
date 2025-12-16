package cn.hjw.dev.platform.app.config;

import cn.hjw.dev.platform.infrastructure.gateway.IWeixinApiGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * 生成 retrofit2 客户端动态代理
 */
@Slf4j
@Configuration
public class Retrofit2Config {

    @Value("${app.config.group-buy-market.api-url}")
    private String groupBuyMarketApiUrl;

    @Bean
    public IWeixinApiGateway weixinApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.weixin.qq.com/")
                .addConverterFactory(JacksonConverterFactory.create()).build();

        return retrofit.create(IWeixinApiGateway.class);
    }

//    @Bean
//    public IGroupBuyMarketService groupBuyMarketService() {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(groupBuyMarketApiUrl)
//                .addConverterFactory(JacksonConverterFactory.create()).build();
//
//        return retrofit.create(IGroupBuyMarketService.class);
//    }
}
