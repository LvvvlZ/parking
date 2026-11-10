package com.parking.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.common.BusinessException;
import com.parking.dto.AdminAssignReq;
import com.parking.dto.AdminSaveReq;
import com.parking.dto.LotSaveReq;
import com.parking.entity.AdminLot;
import com.parking.entity.AdminUser;
import com.parking.entity.ParkingLot;
import com.parking.mapper.AdminLotMapper;
import com.parking.mapper.AdminUserMapper;
import com.parking.mapper.ParkingLotMapper;
import com.parking.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAdminServiceImpl implements SuperAdminService {

    private final AdminUserMapper adminUserMapper;
    private final AdminLotMapper adminLotMapper;
    private final ParkingLotMapper parkingLotMapper;

    @Override
    public AdminUser createAdmin(AdminSaveReq req) {
        Long count = Long.valueOf(adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, req.getUsername())));
        if (count > 0) {
            throw new BusinessException("账号已存在");
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(req.getUsername());
        admin.setPassword(BCrypt.hashpw(req.getPassword()));
        admin.setRealName(req.getRealName());
        admin.setPhone(req.getPhone());
        admin.setRole(AdminUser.ROLE_ADMIN);
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        adminUserMapper.insert(admin);
        admin.setPassword(null);
        return admin;
    }

    @Override
    public List<AdminUser> listAdmins() {
        List<AdminUser> admins = adminUserMapper.selectList(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getRole, AdminUser.ROLE_ADMIN)
                .orderByDesc(AdminUser::getCreateTime));
        admins.forEach(a -> a.setPassword(null));
        return admins;
    }

    @Override
    public void updateAdminStatus(Long adminId, Integer status) {
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null || !AdminUser.ROLE_ADMIN.equals(admin.getRole())) {
            throw new BusinessException("管理员不存在");
        }
        admin.setStatus(status);
        admin.setUpdateTime(LocalDateTime.now());
        adminUserMapper.updateById(admin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignLots(AdminAssignReq req) {
        AdminUser admin = adminUserMapper.selectById(req.getAdminId());
        if (admin == null || !AdminUser.ROLE_ADMIN.equals(admin.getRole())) {
            throw new BusinessException("管理员不存在");
        }
        adminLotMapper.delete(new LambdaQueryWrapper<AdminLot>()
                .eq(AdminLot::getAdminId, req.getAdminId()));
        for (Long lotId : req.getLotIds()) {
            if (parkingLotMapper.selectById(lotId) == null) {
                throw new BusinessException("车场不存在: " + lotId);
            }
            AdminLot al = new AdminLot();
            al.setAdminId(req.getAdminId());
            al.setLotId(lotId);
            al.setCreateTime(LocalDateTime.now());
            adminLotMapper.insert(al);
        }
    }

    @Override
    public List<ParkingLot> adminLots(Long adminId) {
        List<AdminLot> als = adminLotMapper.selectList(new LambdaQueryWrapper<AdminLot>()
                .eq(AdminLot::getAdminId, adminId));
        return als.stream()
                .map(al -> parkingLotMapper.selectById(al.getLotId()))
                .toList();
    }

    @Override
    public ParkingLot createLot(LotSaveReq req) {
        ParkingLot lot = new ParkingLot();
        applyLotReq(lot, req);
        lot.setQrcodeSecret(RandomUtil.randomString(32));
        lot.setCreateTime(LocalDateTime.now());
        lot.setUpdateTime(LocalDateTime.now());
        parkingLotMapper.insert(lot);
        return lot;
    }

    @Override
    public ParkingLot updateLot(Long lotId, LotSaveReq req) {
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null) {
            throw new BusinessException("车场不存在");
        }
        applyLotReq(lot, req);
        parkingLotMapper.updateById(lot);
        return lot;
    }

    @Override
    public void deleteLot(Long lotId) {
        parkingLotMapper.deleteById(lotId);
        adminLotMapper.delete(new LambdaQueryWrapper<AdminLot>().eq(AdminLot::getLotId, lotId));
    }

    private void applyLotReq(ParkingLot lot, LotSaveReq req) {
        lot.setName(req.getName());
        lot.setLocation(req.getLocation());
        lot.setTotalSpaces(req.getTotalSpaces());
        lot.setBindLimit(req.getBindLimit());
        lot.setMonthlyPrice(req.getMonthlyPrice());
        lot.setQuarterlyPrice(req.getQuarterlyPrice());
        lot.setYearlyPrice(req.getYearlyPrice());
        lot.setFreeMinutes(req.getFreeMinutes() == null ? 0 : req.getFreeMinutes());
        lot.setFirstHours(req.getFirstHours() == null ? 1 : req.getFirstHours());
        lot.setFirstPrice(req.getFirstPrice());
        lot.setHourlyPrice(req.getHourlyPrice());
        lot.setDailyMax(req.getDailyMax());
    }
}
