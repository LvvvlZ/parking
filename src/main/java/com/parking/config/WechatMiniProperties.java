package com.parking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置（登录 code2session 用）
 */
@Data
@Component
@ConfigurationProperties(prefix = "parking.wechat-mini")
public class WechatMiniProperties {

    private String appId;

    private String secret;

    /** 模拟登录开关：true 时跳过 code2session，按 code 生成固定 openid（便于本地联调） */
    private boolean mock = false;
}
