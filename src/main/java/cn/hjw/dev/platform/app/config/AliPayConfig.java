package cn.hjw.dev.platform.app.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// 启用配置属性支持，指定配置属性类AliPayConfigProperties
@EnableConfigurationProperties(AliPayConfigProperties.class)
public class AliPayConfig {

    @Bean("alipayClient")
    public AlipayClient alipayClient(AliPayConfigProperties properties) throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl(properties.getGatewayUrl());
        //设置应用ID
        alipayConfig.setAppId(properties.getApp_id());
        //设置应用私钥
        alipayConfig.setPrivateKey(properties.getMerchant_private_key());
        //设置请求格式，固定值json
        alipayConfig.setFormat(properties.getFormat());
        //设置字符集
        alipayConfig.setCharset(properties.getCharset());
        //设置签名类型
        alipayConfig.setSignType(properties.getSign_type());
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey(properties.getAlipay_public_key());
        return new DefaultAlipayClient(alipayConfig); //getSign_type() RSA2用于验签
    }

}
