package com.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.common.BusinessException;
import com.parking.config.WechatPayProperties;
import com.parking.entity.*;
import com.parking.mapper.*;
import com.parking.service.PayOrderService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信支付服务：统一下单 + 回调验签 + 业务闭环
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl implements PayOrderService {

    private final PayOrderMapper payOrderMapper;
    private final WxUserMapper wxUserMapper;
    private final VehicleMapper vehicleMapper;
    private final UserVehicleMapper userVehicleMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final TempParkingRecordMapper tempParkingRecordMapper;
    private final WechatPayProperties props;

    /** 单机锁：同一车辆的续费串行化（多节点部署需换 Redis 分布式锁） */
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private volatile RSAAutoCertificateConfig rsaConfig;
    private volatile JsapiServiceExtension jsapiService;
    private volatile NotificationParser notificationParser;

    private synchronized RSAAutoCertificateConfig rsaConfig() {
        if (rsaConfig == null) {
            rsaConfig = new RSAAutoCertificateConfig.Builder()
                    .merchantId(props.getMerchantId())
                    .privateKeyFromPath(props.getPrivateKeyPath())
                    .merchantSerialNumber(props.getMerchantSerialNumber())
                    .apiV3Key(props.getApiV3Key())
                    .build();
        }
        return rsaConfig;
    }

    private JsapiServiceExtension jsapiService() {
        if (jsapiService == null) {
            jsapiService = new JsapiServiceExtension.Builder()
                    .config(rsaConfig())
                    .build();
        }
        return jsapiService;
    }

    private NotificationParser notificationParser() {
        if (notificationParser == null) {
            notificationParser = new NotificationParser(rsaConfig());
        }
        return notificationParser;
    }

    @Override
    public PrepayWithRequestPaymentResponse createOrder(Long userId, String bizType, Long bizId, String planType) {
        OrderContext ctx = switch (bizType) {
            case PayOrder.BIZ_SUBSCRIPTION -> buildSubscriptionCtx(userId, bizId, planType);
            case PayOrder.BIZ_TEMP_PARKING -> buildTempParkingCtx(bizId);
            default -> throw new BusinessException("不支持的业务类型");
        };

        PayOrder order = new PayOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setBizType(bizType);
        order.setBizId(bizId);
        order.setPlanType(planType);
        order.setAmount(ctx.amountCents());
        order.setStatus(PayOrder.STATUS_CREATED);
        payOrderMapper.insert(order);

        // 模拟支付：不调用微信 SDK，直接返回模拟调起参数（packageVal = mock:orderId）
        if (props.isMock()) {
            order.setStatus(PayOrder.STATUS_PENDING);
            payOrderMapper.updateById(order);
            log.info("[MOCK支付] 模拟下单成功, orderNo={}, amount={}分", order.getOrderNo(), order.getAmount());
            PrepayWithRequestPaymentResponse mock = new PrepayWithRequestPaymentResponse();
            mock.setAppId(props.getAppId());
            mock.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
            mock.setNonceStr("mock" + System.currentTimeMillis());
            mock.setPackageVal("mock:" + order.getId());
            mock.setSignType("RSA");
            mock.setPaySign("MOCK_SIGN");
            return mock;
        }

        PrepayRequest request = new PrepayRequest();
        request.setAppid(props.getAppId());
        request.setMchid(props.getMerchantId());
        request.setDescription(ctx.desc());
        request.setNotifyUrl(props.getNotifyUrl());
        request.setOutTradeNo(order.getOrderNo());
        Amount wxAmount = new Amount();
        wxAmount.setTotal(ctx.amountCents());
        request.setAmount(wxAmount);
        Payer payer = new Payer();
        payer.setOpenid(getOpenid(userId));
        request.setPayer(payer);

        PrepayWithRequestPaymentResponse response = jsapiService().prepayWithRequestPayment(request);

        order.setPrepayId(response.getPackageVal() != null ? extractPrepayId(response.getPackageVal()) : null);
        order.setStatus(PayOrder.STATUS_PENDING);
        payOrderMapper.updateById(order);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePayNotify(String body, String serialNumber, String nonce, String signature, String timestamp) {
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serialNumber)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(body)
                .build();
        Transaction transaction = notificationParser().parse(requestParam, Transaction.class);

        String outTradeNo = transaction.getOutTradeNo();
        PayOrder order = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
                .eq(PayOrder::getOrderNo, outTradeNo));
        if (order == null) {
            log.warn("支付回调订单不存在: {}", outTradeNo);
            return;
        }
        if (PayOrder.STATUS_PAID.equals(order.getStatus())) {
            return; // 幂等
        }
        if (Transaction.TradeStateEnum.SUCCESS == transaction.getTradeState()) {
            order.setStatus(PayOrder.STATUS_PAID);
            order.setTransactionId(transaction.getTransactionId());
            order.setPaidTime(LocalDateTime.now());
            payOrderMapper.updateById(order);
            onPaid(order);
        } else {
            order.setStatus(PayOrder.STATUS_CLOSED);
            payOrderMapper.updateById(order);
        }
    }

    /**
     * 模拟支付成功（仅 mock 开关开启时可用）：复用真实回调后的业务流转
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockPaySuccess(Long orderId, Long userId) {
        if (!props.isMock()) {
            throw new BusinessException("当前未开启模拟支付");
        }
        PayOrder order = payOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该订单");
        }
        if (PayOrder.STATUS_PAID.equals(order.getStatus())) {
            return; // 幂等
        }
        order.setStatus(PayOrder.STATUS_PAID);
        order.setTransactionId("MOCK" + System.currentTimeMillis());
        order.setPaidTime(LocalDateTime.now());
        payOrderMapper.updateById(order);
        log.info("[MOCK支付] 模拟支付成功, orderNo={}", order.getOrderNo());
        onPaid(order);
    }

    // ==================== 业务上下文（服务端计价） ====================

    private record OrderContext(int amountCents, String desc) {}

    private OrderContext buildSubscriptionCtx(Long userId, Long vehicleId, String planType) {
        if (planType == null) {
            throw new BusinessException("套餐类型不能为空");
        }
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        // 绑定校验：绑定人（含车主）均可续费
        Long bound = userVehicleMapper.selectCount(new LambdaQueryWrapper<UserVehicle>()
                .eq(UserVehicle::getUserId, userId)
                .eq(UserVehicle::getVehicleId, vehicleId));
        if (bound == 0) {
            throw new BusinessException("请先绑定该车辆");
        }
        ParkingLot lot = parkingLotMapper.selectById(vehicle.getLotId());
        if (lot == null) {
            throw new BusinessException("车场不存在");
        }
        BigDecimal price = switch (planType) {
            case Subscription.PLAN_MONTH -> lot.getMonthlyPrice();
            case Subscription.PLAN_QUARTER -> lot.getQuarterlyPrice();
            case Subscription.PLAN_YEAR -> lot.getYearlyPrice();
            default -> null;
        };
        if (price == null) {
            throw new BusinessException("该套餐未开放");
        }
        String planLabel = switch (planType) {
            case Subscription.PLAN_MONTH -> "包月";
            case Subscription.PLAN_QUARTER -> "包季";
            case Subscription.PLAN_YEAR -> "包年";
            default -> planType;
        };
        return new OrderContext(price.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).intValue(),
                lot.getName() + "-" + vehicle.getPlateNo() + "-" + planLabel);
    }

    private OrderContext buildTempParkingCtx(Long recordId) {
        TempParkingRecord record = tempParkingRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("临停记录不存在");
        }
        if (record.getPayStatus() != 0) {
            throw new BusinessException("该记录已支付");
        }
        if (record.getFee() == null || record.getExitTime() == null) {
            throw new BusinessException("车辆尚未出场，无法缴费");
        }
        if (record.getFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("该记录无需支付");
        }
        ParkingLot lot = parkingLotMapper.selectById(record.getLotId());
        String desc = (lot != null ? lot.getName() + "-" : "") + record.getPlateNo() + "-临停";
        return new OrderContext(record.getFee().multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).intValue(), desc);
    }

    // ==================== 支付成功业务流转 ====================

    private void onPaid(PayOrder order) {
        switch (order.getBizType()) {
            case PayOrder.BIZ_SUBSCRIPTION -> handleSubscriptionPaid(order);
            case PayOrder.BIZ_TEMP_PARKING -> handleTempParkingPaid(order);
            default -> log.warn("未知业务类型: {}", order.getBizType());
        }
    }

    /**
     * 套餐支付成功：未到期续费顺延 new_expire = max(当前到期, now) + 套餐时长
     */
    private void handleSubscriptionPaid(PayOrder order) {
        ReentrantLock lock = locks.computeIfAbsent("vehicle-" + order.getBizId(), k -> new ReentrantLock());
        lock.lock();
        try {
            Vehicle vehicle = vehicleMapper.selectById(order.getBizId());
            if (vehicle == null) {
                log.error("订阅支付成功但车辆不存在, payOrder={}", order.getId());
                return;
            }
            Subscription latest = subscriptionMapper.selectOne(new LambdaQueryWrapper<Subscription>()
                    .eq(Subscription::getVehicleId, vehicle.getId())
                    .orderByDesc(Subscription::getExpireTime)
                    .last("LIMIT 1"));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime base = (latest != null && latest.getExpireTime().isAfter(now))
                    ? latest.getExpireTime() : now;
            LocalDateTime expire = switch (order.getPlanType()) {
                case Subscription.PLAN_MONTH -> base.plusMonths(1);
                case Subscription.PLAN_QUARTER -> base.plusMonths(3);
                case Subscription.PLAN_YEAR -> base.plusYears(1);
                default -> base;
            };
            Subscription sub = new Subscription();
            sub.setVehicleId(vehicle.getId());
            sub.setLotId(vehicle.getLotId());
            sub.setUserId(order.getUserId());
            sub.setPlanType(order.getPlanType());
            sub.setPrice(BigDecimal.valueOf(order.getAmount(), 2));
            sub.setStartTime(base);
            sub.setExpireTime(expire);
            sub.setCreateTime(now);
            subscriptionMapper.insert(sub);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 临停支付成功：记录置为已支付
     */
    private void handleTempParkingPaid(PayOrder order) {
        TempParkingRecord record = tempParkingRecordMapper.selectById(order.getBizId());
        if (record == null) {
            log.error("临停支付成功但记录不存在, payOrder={}", order.getId());
            return;
        }
        record.setPayStatus(1);
        tempParkingRecordMapper.updateById(record);
        // TODO 通知道闸放行（对接硬件后实现）
    }

    private String getOpenid(Long userId) {
        WxUser user = wxUserMapper.selectById(userId);
        if (user == null || user.getOpenid() == null) {
            throw new BusinessException("用户不存在或未完成微信登录");
        }
        return user.getOpenid();
    }

    private String extractPrepayId(String packageValue) {
        int idx = packageValue.indexOf('=');
        return idx >= 0 ? packageValue.substring(idx + 1) : packageValue;
    }

    private String generateOrderNo() {
        return "PAY" + System.currentTimeMillis()
                + java.util.concurrent.ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
