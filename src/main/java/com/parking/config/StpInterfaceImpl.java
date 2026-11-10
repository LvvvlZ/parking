package com.parking.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.entity.AdminUser;
import com.parking.entity.WxUser;
import com.parking.mapper.AdminUserMapper;
import com.parking.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色/权限数据源：admin_user 中的角色；wx_user 一律为 USER
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final AdminUserMapper adminUserMapper;
    private final WxUserMapper wxUserMapper;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        long id = Long.parseLong(String.valueOf(loginId));
        AdminUser admin = adminUserMapper.selectById(id);
        if (admin != null) {
            return List.of(admin.getRole());
        }
        WxUser wxUser = wxUserMapper.selectById(id);
        if (wxUser != null) {
            return List.of("USER");
        }
        return List.of();
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }
}
