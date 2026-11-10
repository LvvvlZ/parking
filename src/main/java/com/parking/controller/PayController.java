package com.parking.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.parking.common.Result;
import com.parking.dto.PayOrderReq;
import com.parking.service.PayOrderService;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayOrderService payOrderService;

    /**
     * 创建支付订单（服务端根据 bizType 计价），返回小程序 wx.requestPayment 所需参数
     */
    @PostMapping("/order")
    public Result<PrepayWithRequestPaymentResponse> createOrder(@Valid @RequestBody PayOrderReq req) {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(payOrderService.createOrder(userId, req.getBizType(), req.getBizId(), req.getPlanType()));
    }

    /**
     * 模拟支付成功（仅 parking.wechat-pay.mock=true 时可用）
     * 前端识别到 packageVal 以 "mock:" 开头时，调用本接口代替真实支付
     */
    @PostMapping("/mock/notify")
    public Result<Void> mockPayNotify(@RequestBody Map<String, Object> body) {
        StpUtil.checkLogin();
        Long orderId = Long.valueOf(String.valueOf(body.get("orderId")));
        payOrderService.mockPaySuccess(orderId, StpUtil.getLoginIdAsLong());
        return Result.ok();
    }

    /**
     * 微信支付回调通知（微信服务器调用，公网可访问，免登录）
     */
    @PostMapping("/notify")
    public Map<String, String> payNotify(HttpServletRequest request) {
        try {
            String body = new BufferedReader(request.getReader())
                    .lines().collect(Collectors.joining("\n"));
            payOrderService.handlePayNotify(
                    body,
                    request.getHeader("Wechatpay-Serial"),
                    request.getHeader("Wechatpay-Nonce"),
                    request.getHeader("Wechatpay-Signature"),
                    request.getHeader("Wechatpay-Timestamp"));
            return Map.of("code", "SUCCESS", "message", "成功");
        } catch (Exception e) {
            return Map.of("code", "FAIL", "message", "失败");
        }
    }
}
