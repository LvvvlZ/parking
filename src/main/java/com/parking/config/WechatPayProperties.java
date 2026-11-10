package com.parking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置（application.yml -> parking.wechat-pay）
 */
@Data
@Component
@ConfigurationProperties(prefix = "parking.wechat-pay")
public class WechatPayProperties {

    /** 小程序 appId */
    private String appId;

    /** 商户号 */
    private String merchantId;

    /** API v3 密钥 */
    private String apiV3Key;

    /** 商户 API 证书序列号 */
    private String merchantSerialNumber;

    /** 商户私钥文件路径（apiclient_key.pem） */
    private String privateKeyPath;

    /** 支付回调通知地址（必须为 https 公网地址） */
    private String notifyUrl;

    /** 模拟支付开关：true 时不调用真实微信支付，返回模拟参数并提供模拟支付成功接口 */
    private boolean mock = false;
}
