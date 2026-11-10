package com.parking.service;

import com.parking.dto.AdminLoginReq;
import com.parking.dto.LoginVO;
import com.parking.dto.WxLoginReq;

public interface AuthService {

    /** 后台管理员登录（账号密码） */
    LoginVO adminLogin(AdminLoginReq req);

    /** 小程序登录（code2session，首次登录自动注册） */
    LoginVO wxLogin(WxLoginReq req);
}
