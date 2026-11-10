package com.parking.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.common.BusinessException;
import com.parking.dto.SubscriptionVO;
import com.parking.entity.Subscription;
import com.parking.entity.UserVehicle;
import com.parking.entity.Vehicle;
import com.parking.mapper.SubscriptionMapper;
import com.parking.mapper.UserVehicleMapper;
import com.parking.mapper.VehicleMapper;
import com.parking.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;
    private final VehicleMapper vehicleMapper;
    private final UserVehicleMapper userVehicleMapper;

    @Override
    public List<SubscriptionVO> mySubscriptions() {
        Long userId = StpUtil.getLoginIdAsLong();
        return subscriptionMapper.selectList(new LambdaQueryWrapper<Subscription>()
                        .eq(Subscription::getUserId, userId)
                        .orderByDesc(Subscription::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<SubscriptionVO> vehicleSubscriptions(Long vehicleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long bound = userVehicleMapper.selectCount(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getUserId, userId)
                .eq(UserVehicle::getVehicleId, vehicleId));
        if (bound == 0) {
            throw new BusinessException("请先绑定该车辆");
        }
        return subscriptionMapper.selectList(new LambdaQueryWrapper<Subscription>()
                        .eq(Subscription::getVehicleId, vehicleId)
                        .orderByDesc(Subscription::getExpireTime))
                .stream().map(this::toVO).toList();
    }

    private SubscriptionVO toVO(Subscription sub) {
        SubscriptionVO vo = new SubscriptionVO();
        vo.setId(sub.getId());
        vo.setVehicleId(sub.getVehicleId());
        Vehicle vehicle = vehicleMapper.selectById(sub.getVehicleId());
        vo.setPlateNo(vehicle != null ? vehicle.getPlateNo() : null);
        vo.setPlanType(sub.getPlanType());
        vo.setPrice(sub.getPrice());
        vo.setStartTime(sub.getStartTime());
        vo.setExpireTime(sub.getExpireTime());
        return vo;
    }
}
