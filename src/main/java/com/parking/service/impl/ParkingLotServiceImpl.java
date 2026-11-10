package com.parking.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.common.BusinessException;
import com.parking.dto.LotVO;
import com.parking.entity.AdminLot;
import com.parking.entity.AdminUser;
import com.parking.entity.ParkingLot;
import com.parking.entity.Vehicle;
import com.parking.mapper.AdminLotMapper;
import com.parking.mapper.AdminUserMapper;
import com.parking.mapper.ParkingLotMapper;
import com.parking.mapper.VehicleMapper;
import com.parking.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingLotServiceImpl implements ParkingLotService {

    private final ParkingLotMapper parkingLotMapper;
    private final AdminLotMapper adminLotMapper;
    private final AdminUserMapper adminUserMapper;
    private final VehicleMapper vehicleMapper;

    @Override
    public List<LotVO> myLots() {
        AdminUser admin = adminUserMapper.selectById(StpUtil.getLoginIdAsLong());
        List<ParkingLot> lots;
        if (AdminUser.ROLE_SUPER.equals(admin.getRole())) {
            lots = parkingLotMapper.selectList(new LambdaQueryWrapper<ParkingLot>()
                    .orderByDesc(ParkingLot::getCreateTime));
        } else {
            List<Long> lotIds = adminLotMapper.selectList(new LambdaQueryWrapper<AdminLot>()
                            .eq(AdminLot::getAdminId, admin.getId()))
                    .stream().map(AdminLot::getLotId).toList();
            if (lotIds.isEmpty()) {
                return List.of();
            }
            lots = parkingLotMapper.selectBatchIds(lotIds);
        }
        return lots.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public LotVO detail(Long lotId) {
        checkLotPermission(lotId);
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null) {
            throw new BusinessException("车场不存在");
        }
        return toVO(lot);
    }

    @Override
    public LotVO publicDetail(Long lotId) {
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null) {
            throw new BusinessException("车场不存在");
        }
        return toVO(lot);
    }

    @Override
    public void checkLotPermission(Long lotId) {
        AdminUser admin = adminUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (admin == null) {
            throw new BusinessException(403, "无权管理该车场");
        }
        if (AdminUser.ROLE_SUPER.equals(admin.getRole())) {
            return;
        }
        Long count = adminLotMapper.selectCount(new LambdaQueryWrapper<AdminLot>()
                .eq(AdminLot::getAdminId, admin.getId())
                .eq(AdminLot::getLotId, lotId));
        if (count == 0) {
            throw new BusinessException(403, "无权管理该车场");
        }
    }

    @Override
    public String qrcodePayload(Long lotId) {
        checkLotPermission(lotId);
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null) {
            throw new BusinessException("车场不存在");
        }
        return lotId + ":" + SecureUtil.md5(lotId + lot.getQrcodeSecret());
    }

    @Override
    public void verifyScan(Long lotId, String sign) {
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null) {
            throw new BusinessException("二维码无效");
        }
        if (!SecureUtil.md5(lotId + lot.getQrcodeSecret()).equals(sign)) {
            throw new BusinessException("二维码校验失败");
        }
    }

    private LotVO toVO(ParkingLot lot) {
        LotVO vo = new LotVO();
        vo.setId(lot.getId());
        vo.setName(lot.getName());
        vo.setLocation(lot.getLocation());
        vo.setTotalSpaces(lot.getTotalSpaces());
        vo.setBindLimit(lot.getBindLimit());
        vo.setMonthlyPrice(lot.getMonthlyPrice());
        vo.setQuarterlyPrice(lot.getQuarterlyPrice());
        vo.setYearlyPrice(lot.getYearlyPrice());
        vo.setFreeMinutes(lot.getFreeMinutes());
        vo.setFirstHours(lot.getFirstHours());
        vo.setFirstPrice(lot.getFirstPrice());
        vo.setHourlyPrice(lot.getHourlyPrice());
        vo.setDailyMax(lot.getDailyMax());
        vo.setCreateTime(lot.getCreateTime());
        // 绑定车辆数
        Long bindCount = vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getLotId, lot.getId()));
        vo.setBindCount(bindCount.intValue());
        return vo;
    }
}
