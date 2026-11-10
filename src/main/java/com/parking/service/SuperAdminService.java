package com.parking.service;

import com.parking.dto.AdminSaveReq;
import com.parking.dto.AdminAssignReq;
import com.parking.dto.LotSaveReq;
import com.parking.entity.AdminUser;
import com.parking.entity.ParkingLot;

import java.util.List;

public interface SuperAdminService {

    // 管理员
    AdminUser createAdmin(AdminSaveReq req);

    List<AdminUser> listAdmins();

    void updateAdminStatus(Long adminId, Integer status);

    /** 全量覆盖管理员的车场分配 */
    void assignLots(AdminAssignReq req);

    List<ParkingLot> adminLots(Long adminId);

    // 车场
    ParkingLot createLot(LotSaveReq req);

    ParkingLot updateLot(Long lotId, LotSaveReq req);

    void deleteLot(Long lotId);
}
