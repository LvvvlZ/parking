package com.parking.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.parking.common.Result;
import com.parking.dto.VehicleAddReq;
import com.parking.dto.VehicleVO;
import com.parking.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /** 用户端：扫码添加车辆（默认绑定扫码车场） */
    @PostMapping("/add")
    public Result<VehicleVO> add(@Valid @RequestBody VehicleAddReq req) {
        return Result.ok(vehicleService.addVehicle(req));
    }

    /** 用户端：我的车辆 */
    @GetMapping("/my")
    public Result<List<VehicleVO>> my() {
        return Result.ok(vehicleService.myVehicles());
    }

    /** 车辆详情 */
    @GetMapping("/{id}")
    public Result<VehicleVO> detail(@PathVariable Long id) {
        return Result.ok(vehicleService.detail(id));
    }

    /** 用户端：解绑 */
    @PostMapping("/unbind")
    public Result<Void> unbind(@RequestBody Map<String, Long> req) {
        vehicleService.unbind(req.get("vehicleId"));
        return Result.ok();
    }

    /** 管理端：车场车辆列表 */
    @GetMapping("/lot/{lotId}/list")
    public Result<List<VehicleVO>> lotVehicles(@PathVariable Long lotId) {
        return Result.ok(vehicleService.lotVehicles(lotId));
    }

    /** 管理端：解绑某用户与车辆的绑定 */
    @PostMapping("/lot/unbind")
    public Result<Void> adminUnbind(@RequestBody Map<String, Long> req) {
        vehicleService.adminUnbind(req.get("vehicleId"), req.get("userId"));
        return Result.ok();
    }
}
