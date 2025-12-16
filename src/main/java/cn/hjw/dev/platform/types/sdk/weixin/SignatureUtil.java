package cn.hjw.dev.platform.types.sdk.weixin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignatureUtil {
    /**
     * 验证签名
     * @param token     微信公众平台上设置的token
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param signature 微信服务器传递过来的签名
     * @return 验证结果
     */
    /*
    * SHA（Secure Hash Algorithm，安全哈希算法）是由美国国家标准与技术研究院（NIST）设计的密码学哈希函数家族，
    * 核心作用是将任意长度的输入数据映射为固定长度的哈希值（摘要），具有「不可逆」「抗篡改」（微小输入变化会导致哈希值完全不同）的核心特性。
    * SHA-1 和 SHA-2 是该家族中最具代表性的两个版本，其中 SHA-2 是 SHA-1 的升级版，安全性和应用场景差异显著。
    * SHA-1（SHA-1995）
    *输出长度：固定 160 位（20 字节），对应 40 位十六进制字符串（比如 da39a3ee5e6b4b0d3255bfef95601890afd80709 是空字符串的 SHA-1 哈希）
    * 处理逻辑：将输入数据分块（每块 512 位），通过初始向量（IV）、80 轮位运算（旋转、异或、与、或等）迭代处理每个块，最终生成 160 位摘要。
    * SHA-1 的致命问题是抗碰撞性失效：
    * 哈希函数的「抗碰撞性」要求「无法找到两个不同的输入，得到相同的哈希值」
    *
    * SHA-2（SHA-2 系列，2001 年发布）
    * SHA-2 不是单一算法，而是一个算法家族，是 SHA-1 的升级版，核心解决了 SHA-1 的安全性缺陷。
    * SHA-2 家族包括多个变体，最常用的是 SHA-256 和 SHA-512：
    * SHA-256：输出长度 256 位（32 字节），对应 64 位十六进制字符串
    * SHA-512：输出长度 512 位（64 字节），对应 128 位十六进制字符串
    * 处理逻辑：
    * 与 SHA-1 类似，但使用更复杂的初始向量、更长的处理块（SHA-256 使用 512 位块，SHA-512 使用 1024 位块），
    * 以及更多轮次的位运算（SHA-256 有 64 轮，SHA-512 有 80 轮）。
    * SHA-2 目前被广泛认为是安全的，尚未发现实用的碰撞攻击方法，因此在安全敏感的应用中被广泛采用，如 SSL/TLS 证书签名、数字签名和数据完整性验证等。
    * 截至 2025 年，SHA-2 系列（尤其是 SHA-256/SHA-512）无实际可行的碰撞攻击，是 NIST 推荐的「安全哈希标准」；
    * */
    public static boolean check(String token, String timestamp, String nonce, String signature) {
        String[] arr = new String[]{token, timestamp, nonce};
        // 将token、timestamp、nonce三个参数进行字典序排序
        sort(arr);
        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }
        MessageDigest md;
        String tmpStr = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            // 将三个参数字符串拼接成一个字符串进行sha1加密，获取40位十六进制字符串
            byte[] digest = md.digest(content.toString().getBytes());
            tmpStr = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
        return tmpStr != null && tmpStr.equals(signature.toUpperCase());
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String byteToStr(byte[] byteArray) {
        StringBuilder strDigest = new StringBuilder();
        for (byte b : byteArray) {
            strDigest.append(byteToHexStr(b));
        }
        return strDigest.toString();
    }

    /**
     * 将字节转换为十六进制字符串
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F]; // >>> 无符号右移4位，获取高4位
        tempArr[1] = Digit[mByte & 0X0F]; // 获取低4位
        return new String(tempArr);
    }

    /**
     * 进行字典排序,插入排序法
     * @param str 字符串数组
     */
    private static void sort(String[] str) {
        for (int i = 0; i < str.length - 1; i++) {
            for (int j = i + 1; j < str.length; j++) {
                if (str[j].compareTo(str[i]) < 0) {
                    String temp = str[i];
                    str[i] = str[j];
                    str[j] = temp;
                }
            }
        }
    }
}
