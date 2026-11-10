package com.parking.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.parking.common.Result;
import com.parking.dto.LotVO;
import com.parking.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    /** 管理端：我的车场（超管=全部，普通管理=分配的） */
    @GetMapping("/lot-admin/my")
    public Result<?> myLots() {
        return Result.ok(parkingLotService.myLots());
    }

    /** 管理端：车场详情 */
    @GetMapping("/lot-admin/{lotId}")
    public Result<LotVO> detail(@PathVariable Long lotId) {
        return Result.ok(parkingLotService.detail(lotId));
    }

    /** 管理端：车场二维码内容 */
    @GetMapping("/lot-admin/{lotId}/qrcode")
    public Result<Map<String, String>> qrcode(@PathVariable Long lotId) {
        return Result.ok(Map.of("payload", parkingLotService.qrcodePayload(lotId)));
    }

    /** 用户端（公开）：扫码校验并返回车场信息 */
    @GetMapping("/lot/scan")
    public Result<LotVO> scan(String lotId, String sign) {
        parkingLotService.verifyScan(Long.valueOf(lotId), sign);
        return Result.ok(parkingLotService.publicDetail(Long.valueOf(lotId)));
    }
}
