package com.parking.controller;

import com.parking.common.Result;
import com.parking.dto.TempFeeVO;
import com.parking.dto.TempGateReq;
import com.parking.entity.TempParkingRecord;
import com.parking.service.TempParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 临停进出（道闸/车牌识别回调，车场签名鉴权，免登录）
 */
@RestController
@RequestMapping("/api/temp")
@RequiredArgsConstructor
public class TempParkingController {

    private final TempParkingService tempParkingService;

    /** 入场 */
    @PostMapping("/entry")
    public Result<Long> entry(@Valid @RequestBody TempGateReq req) {
        TempParkingRecord record = tempParkingService.entry(req);
        return Result.ok(record.getId());
    }

    /** 出场：计费，返回费用信息（之后用户经 /api/pay/order 缴费） */
    @PostMapping("/exit")
    public Result<TempFeeVO> exit(@Valid @RequestBody TempGateReq req) {
        return Result.ok(tempParkingService.exit(req));
    }
}
