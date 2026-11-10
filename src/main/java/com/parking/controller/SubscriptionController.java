package com.parking.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.parking.common.Result;
import com.parking.dto.SubscribeReq;
import com.parking.dto.SubscriptionVO;
import com.parking.service.PayOrderService;
import com.parking.service.SubscriptionService;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PayOrderService payOrderService;

    /** 订阅/续费：服务端计价，返回小程序调起支付参数 */
    @PostMapping("/order")
    public Result<PrepayWithRequestPaymentResponse> subscribe(@Valid @RequestBody SubscribeReq req) {
        StpUtil.checkLogin();
        return Result.ok(payOrderService.createOrder(
                StpUtil.getLoginIdAsLong(), "SUBSCRIPTION", req.getVehicleId(), req.getPlanType()));
    }

    /** 我的订阅记录 */
    @GetMapping("/my")
    public Result<List<SubscriptionVO>> my() {
        return Result.ok(subscriptionService.mySubscriptions());
    }

    /** 某车辆的订阅记录 */
    @GetMapping("/vehicle/{vehicleId}")
    public Result<List<SubscriptionVO>> byVehicle(@PathVariable Long vehicleId) {
        return Result.ok(subscriptionService.vehicleSubscriptions(vehicleId));
    }
}
