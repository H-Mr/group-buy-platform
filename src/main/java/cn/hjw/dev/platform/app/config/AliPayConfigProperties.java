package cn.hjw.dev.platform.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "alipay", ignoreInvalidFields = true)
public class AliPayConfigProperties {

    // 「沙箱环境」应用ID - 您的APPID，收款账号既是你的APPID对应支付宝账号。获取地址；https://open.alipay.com/develop/sandbox/app
    private String app_id;
    // 开发者私钥，由开发者自己生成。，你的PKCS8格式RSA2私钥
    private String merchant_private_key;
    // 支付宝公钥，由支付宝生成。
    private String alipay_public_key;
    // 「沙箱环境」服务器异步通知页面路径
    private String notify_url;
    // 「沙箱环境」页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private String return_url;
    // 支付宝网关（固定）。
    private String gatewayUrl;
    // 签名方式
    private String sign_type = "RSA2";
    // 编码集，支持 GBK/UTF-8。
    private String charset = "utf-8";
    // 参数返回格式，只支持 JSON 格式（固定）。
    private String format = "json";

}
/*
 * RSA 是 1977 年由罗纳德・李维斯特（Ron Rivest）、阿迪・萨莫尔（Adi Shamir）和伦纳德・阿德曼（Leonard Adleman）提出的非对称加密算法，
 * 也是目前应用最广泛的公钥密码算法之一。其核心特点是「双钥体系」（公钥 + 私钥）：公钥可公开，用于加密 验签；
 * 私钥需保密，用于解密签名，安全性基于「大整数质因数分解.标准密码学体系中无「RSA1/RSA2」的官方算法版本定义，这两个术语主要是国内业务场景（如支付宝 |微信支付开放平台）对 RSA 签名组合方式的分类，而非算法本身的迭代版本，下文会重点区分。
 *RSA 分为「密钥生成」「加密」「解密」「签名验签」四大步骤

 维度	        RSA1（RSA-SHA1）	    RSA2（RSA-SHA256）
核心定义 	RSA 签名 + SHA1 哈希算法	    RSA 签名 + SHA256 哈希算法
哈希长度	    160 位（SHA1）	            256 位（SHA256）
安全性	    低（SHA1 已被破解，存在碰撞）	高（SHA256 无实际破解案例）
应用要求	    支付宝仅兼容，不推荐	        支付宝强制要求新应用使用
核心场景	    老旧系统兼容	            支付接口、API 签名、数字证书等

两者的 RSA 密钥生成、加密 / 解密逻辑完全一致，仅「签名环节的哈希算法」不同；
SHA1 的碰撞漏洞导致 RSA1 的签名可被伪造，因此 2015 年后主流平台（支付宝、微信支付、银联）均要求使用 RSA2；
除了 RSA1/RSA2，还有 RSA-SHA512（更高安全级），但 RSA2（SHA256）是当前主流。
 */

