package com.parking.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.parking.common.Result;
import com.parking.dto.AdminAssignReq;
import com.parking.dto.AdminSaveReq;
import com.parking.dto.LotSaveReq;
import com.parking.entity.AdminUser;
import com.parking.entity.ParkingLot;
import com.parking.service.SuperAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 超级管理接口（仅 SUPER_ADMIN）
 */
@RestController
@RequestMapping("/api/super")
@RequiredArgsConstructor
@SaCheckRole("SUPER_ADMIN")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @PostMapping("/admin")
    public Result<AdminUser> createAdmin(@Valid @RequestBody AdminSaveReq req) {
        return Result.ok(superAdminService.createAdmin(req));
    }

    @GetMapping("/admin/list")
    public Result<List<AdminUser>> listAdmins() {
        return Result.ok(superAdminService.listAdmins());
    }

    @PostMapping("/admin/status")
    public Result<Void> updateAdminStatus(@RequestBody Map<String, Object> req) {
        Long id = Long.valueOf(String.valueOf(req.get("id")));
        Integer status = Integer.valueOf(String.valueOf(req.get("status")));
        superAdminService.updateAdminStatus(id, status);
        return Result.ok();
    }

    @PostMapping("/admin/assign")
    public Result<Void> assignLots(@Valid @RequestBody AdminAssignReq req) {
        superAdminService.assignLots(req);
        return Result.ok();
    }

    @GetMapping("/admin/{adminId}/lots")
    public Result<List<ParkingLot>> adminLots(@PathVariable Long adminId) {
        return Result.ok(superAdminService.adminLots(adminId));
    }

    @PostMapping("/lot")
    public Result<ParkingLot> createLot(@Valid @RequestBody LotSaveReq req) {
        return Result.ok(superAdminService.createLot(req));
    }

    @PutMapping("/lot/{lotId}")
    public Result<ParkingLot> updateLot(@PathVariable Long lotId, @Valid @RequestBody LotSaveReq req) {
        return Result.ok(superAdminService.updateLot(lotId, req));
    }

    @DeleteMapping("/lot/{lotId}")
    public Result<Void> deleteLot(@PathVariable Long lotId) {
        superAdminService.deleteLot(lotId);
        return Result.ok();
    }
}
