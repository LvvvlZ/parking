package com.parking.controller;

import com.parking.common.Result;
import com.parking.dto.AdminLoginReq;
import com.parking.dto.LoginVO;
import com.parking.dto.WxLoginReq;
import com.parking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 后台登录 */
    @PostMapping("/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody AdminLoginReq req) {
        return Result.ok(authService.adminLogin(req));
    }

    /** 小程序登录 */
    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@Valid @RequestBody WxLoginReq req) {
        return Result.ok(authService.wxLogin(req));
    }
}
