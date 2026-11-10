package com.parking.service;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

/**
 * 支付服务（金额由服务端根据业务计算，不接受前端金额）
 */
public interface PayOrderService {

    /**
     * 创建支付订单并调用微信下单，返回小程序调起支付所需参数
     *
     * @param userId   付款用户 wx_user.id
     * @param bizType  SUBSCRIPTION（套餐，bizId=vehicleId）/ TEMP_PARKING（临停，bizId=记录ID）
     * @param bizId    业务ID
     * @param planType 套餐类型（bizType=SUBSCRIPTION 时必填）
     */
    PrepayWithRequestPaymentResponse createOrder(Long userId, String bizType, Long bizId, String planType);

    /**
     * 处理微信支付回调通知（验签 + 更新订单状态 + 业务流转）
     */
    void handlePayNotify(String body, String serialNumber, String nonce, String signature, String timestamp);

    /**
     * 模拟支付成功（仅 mock 开关开启时可用）：跳过真实微信支付，直接走支付成功业务流转
     *
     * @param orderId pay_order.id
     * @param userId  当前登录用户（校验订单归属）
     */
    void mockPaySuccess(Long orderId, Long userId);
}
