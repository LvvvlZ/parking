package com.parking.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.common.BusinessException;
import com.parking.dto.VehicleAddReq;
import com.parking.dto.VehicleVO;
import com.parking.entity.ParkingLot;
import com.parking.entity.Subscription;
import com.parking.entity.UserVehicle;
import com.parking.entity.Vehicle;
import com.parking.mapper.ParkingLotMapper;
import com.parking.mapper.SubscriptionMapper;
import com.parking.mapper.UserVehicleMapper;
import com.parking.mapper.VehicleMapper;
import com.parking.service.ParkingLotService;
import com.parking.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleMapper vehicleMapper;
    private final UserVehicleMapper userVehicleMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final ParkingLotService parkingLotService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleVO addVehicle(VehicleAddReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 防伪校验
        parkingLotService.verifyScan(req.getLotId(), req.getSign());
        ParkingLot lot = parkingLotMapper.selectById(req.getLotId());
        if (lot == null) {
            throw new BusinessException("车场不存在");
        }
        // 车位数约束
        if (lot.getBindLimit() != null) {
            Long bound = vehicleMapper.selectCount(new LambdaQueryWrapper<Vehicle>()
                    .eq(Vehicle::getLotId, lot.getId()));
            if (bound >= lot.getBindLimit()) {
                throw new BusinessException("该车场绑定车辆数已达上限");
            }
        }
        Vehicle vehicle = vehicleMapper.selectOne(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getPlateNo, req.getPlateNo()));
        if (vehicle == null) {
            vehicle = new Vehicle();
            vehicle.setPlateNo(req.getPlateNo());
            vehicle.setOwnerId(userId);
            vehicle.setLotId(lot.getId());
            vehicle.setCreateTime(LocalDateTime.now());
            vehicle.setUpdateTime(LocalDateTime.now());
            vehicleMapper.insert(vehicle);
        }
        // 建立用户-车辆绑定（幂等）
        Long exists = userVehicleMapper.selectCount(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getUserId, userId)
                .eq(UserVehicle::getVehicleId, vehicle.getId()));
        if (exists == 0) {
            UserVehicle uv = new UserVehicle();
            uv.setUserId(userId);
            uv.setVehicleId(vehicle.getId());
            uv.setIsOwner(Objects.equals(vehicle.getOwnerId(), userId) ? 1 : 0);
            uv.setBindTime(LocalDateTime.now());
            userVehicleMapper.insert(uv);
        }
        return detail(vehicle.getId());
    }

    @Override
    public List<VehicleVO> myVehicles() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> vehicleIds = userVehicleMapper.selectList(new LambdaQueryWrapper<UserVehicle>()
                        .eq(UserVehicle::getUserId, userId))
                .stream().map(UserVehicle::getVehicleId).toList();
        if (vehicleIds.isEmpty()) {
            return List.of();
        }
        return vehicleMapper.selectBatchIds(vehicleIds).stream()
                .map(v -> toVO(v, userId))
                .toList();
    }

    @Override
    public VehicleVO detail(Long vehicleId) {
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        return toVO(vehicle, StpUtil.getLoginIdAsLong());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long vehicleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        UserVehicle uv = userVehicleMapper.selectOne(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getUserId, userId)
                .eq(UserVehicle::getVehicleId, vehicleId));
        if (uv == null) {
            throw new BusinessException("您未绑定该车辆");
        }
        if (!Objects.equals(vehicle.getOwnerId(), userId)) {
            // 非车主：仅可解绑自己
            doUnbind(userId, vehicleId);
            return;
        }
        // 车主：解绑自己时先转移车主给最早绑定的其他使用人
        List<UserVehicle> others = userVehicleMapper.selectList(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getVehicleId, vehicleId)
                .ne(UserVehicle::getUserId, userId)
                .orderByAsc(UserVehicle::getBindTime));
        if (!others.isEmpty()) {
            vehicle.setOwnerId(others.get(0).getUserId());
            vehicle.setUpdateTime(LocalDateTime.now());
            vehicleMapper.updateById(vehicle);
        } else {
            // 没有其他绑定人：车辆作废
            vehicleMapper.deleteById(vehicleId);
        }
        doUnbind(userId, vehicleId);
    }

    @Override
    public List<VehicleVO> lotVehicles(Long lotId) {
        parkingLotService.checkLotPermission(lotId);
        List<Vehicle> vehicles = vehicleMapper.selectList(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getLotId, lotId)
                .orderByDesc(Vehicle::getCreateTime));
        return vehicles.stream().map(v -> toVO(v, null)).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUnbind(Long vehicleId, Long userId) {
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        parkingLotService.checkLotPermission(vehicle.getLotId());
        doUnbind(userId, vehicleId);
        Long remains = userVehicleMapper.selectCount(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getVehicleId, vehicleId));
        if (remains == 0) {
            vehicleMapper.deleteById(vehicleId);
        } else if (Objects.equals(vehicle.getOwnerId(), userId)) {
            UserVehicle next = userVehicleMapper.selectList(new LambdaQueryWrapper<UserVehicle>()
                            .eq(UserVehicle::getVehicleId, vehicleId)
                            .orderByAsc(UserVehicle::getBindTime))
                    .get(0);
            vehicle.setOwnerId(next.getUserId());
            vehicle.setUpdateTime(LocalDateTime.now());
            vehicleMapper.updateById(vehicle);
        }
    }

    private void doUnbind(Long userId, Long vehicleId) {
        userVehicleMapper.delete(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getUserId, userId)
                .eq(UserVehicle::getVehicleId, vehicleId));
    }

    private VehicleVO toVO(Vehicle vehicle, Long viewerId) {
        VehicleVO vo = new VehicleVO();
        vo.setId(vehicle.getId());
        vo.setPlateNo(vehicle.getPlateNo());
        vo.setLotId(vehicle.getLotId());
        ParkingLot lot = parkingLotMapper.selectById(vehicle.getLotId());
        vo.setLotName(lot != null ? lot.getName() : null);
        if (viewerId != null) {
            UserVehicle uv = userVehicleMapper.selectOne(new LambdaQueryWrapper<UserVehicle>()
                    .eq(UserVehicle::getUserId, viewerId)
                    .eq(UserVehicle::getVehicleId, vehicle.getId()));
            vo.setIsOwner(uv != null && uv.getIsOwner() == 1);
        }
        // 最新订阅
        Subscription latest = subscriptionMapper.selectOne(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getVehicleId, vehicle.getId())
                .orderByDesc(Subscription::getExpireTime)
                .last("LIMIT 1"));
        if (latest == null) {
            vo.setSubStatus("NONE");
        } else {
            vo.setLatestExpire(latest.getExpireTime());
            vo.setPlanType(latest.getPlanType());
            vo.setSubStatus(latest.getExpireTime().isAfter(LocalDateTime.now()) ? "VALID" : "EXPIRED");
        }
        vo.setUserIds(userVehicleMapper.selectList(new LambdaQueryWrapper<UserVehicle>()
                        .eq(UserVehicle::getVehicleId, vehicle.getId())
                        .orderByDesc(UserVehicle::getIsOwner)
                        .orderByAsc(UserVehicle::getBindTime))
                .stream().map(UserVehicle::getUserId).toList());
        return vo;
    }
}
