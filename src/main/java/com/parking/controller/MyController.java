package com.parking.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.parking.common.Result;
import com.parking.entity.WxUser;
import com.parking.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序用户侧：个人资料
 */
@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyController {

    private final WxUserMapper wxUserMapper;

    @GetMapping("/profile")
    public Result<WxUser> profile() {
        StpUtil.checkLogin();
        WxUser user = wxUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user != null) {
            user.setOpenid(null);
            user.setUnionid(null);
        }
        return Result.ok(user);
    }
}
