package com.parking.service;

import com.parking.dto.VehicleAddReq;
import com.parking.dto.VehicleVO;

import java.util.List;

public interface VehicleService {

    /** 用户扫码添加车辆（默认绑定扫码车场，车牌已存在则绑定该车场） */
    VehicleVO addVehicle(VehicleAddReq req);

    /** 我绑定/拥有的车辆 */
    List<VehicleVO> myVehicles();

    VehicleVO detail(Long vehicleId);

    /** 解绑：本人可解绑自己；车主可解绑他人；车主解绑自己时转移车主 */
    void unbind(Long vehicleId);

    // 管理端
    List<VehicleVO> lotVehicles(Long lotId);

    void adminUnbind(Long vehicleId, Long userId);
}
