package com.parking.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.common.BusinessException;
import com.parking.config.WechatMiniProperties;
import com.parking.dto.AdminLoginReq;
import com.parking.dto.LoginVO;
import com.parking.dto.WxLoginReq;
import com.parking.entity.AdminUser;
import com.parking.entity.WxUser;
import com.parking.mapper.AdminUserMapper;
import com.parking.mapper.WxUserMapper;
import com.parking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String JSCODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final AdminUserMapper adminUserMapper;
    private final WxUserMapper wxUserMapper;
    private final WechatMiniProperties miniProps;

    @Override
    public LoginVO adminLogin(AdminLoginReq req) {
        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, req.getUsername()));
        if (admin == null || !BCrypt.checkpw(req.getPassword(), admin.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (admin.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        StpUtil.login(admin.getId());
        return new LoginVO(StpUtil.getTokenValue(), admin.getId(), admin.getRole(), admin.getRealName());
    }

    @Override
    public LoginVO wxLogin(WxLoginReq req) {
        String openid = code2openid(req.getCode());
        WxUser user = wxUserMapper.selectOne(new LambdaQueryWrapper<WxUser>()
                .eq(WxUser::getOpenid, openid));
        if (user == null) {
            user = new WxUser();
            user.setOpenid(openid);
            user.setNickname(req.getNickname());
            user.setAvatar(req.getAvatar());
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            wxUserMapper.insert(user);
        } else if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        } else if (req.getNickname() != null || req.getAvatar() != null) {
            if (req.getNickname() != null) {
                user.setNickname(req.getNickname());
            }
            if (req.getAvatar() != null) {
                user.setAvatar(req.getAvatar());
            }
            user.setUpdateTime(LocalDateTime.now());
            wxUserMapper.updateById(user);
        }
        StpUtil.login(user.getId());
        return new LoginVO(StpUtil.getTokenValue(), user.getId(), "USER", user.getNickname());
    }

    private String code2openid(String code) {
        // 模拟登录：按 code 生成稳定 openid（同一 code 值同一用户），无需真实 appid/secret
        if (miniProps.isMock()) {
            log.info("[MOCK登录] code={} -> openid=mock_{}", code, cn.hutool.crypto.SecureUtil.md5(code).substring(0, 16));
            return "mock_" + cn.hutool.crypto.SecureUtil.md5(code).substring(0, 16);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("appid", miniProps.getAppId());
        params.put("secret", miniProps.getSecret());
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");
        String resp = HttpUtil.get(JSCODE2SESSION_URL, params, 5000);
        JSONObject json = JSONUtil.parseObj(resp);
        if (json.getInt("errcode", 0) != 0) {
            log.warn("jscode2session 失败: {}", resp);
            throw new BusinessException("微信登录失败，请重试");
        }
        return json.getStr("openid");
    }
}
