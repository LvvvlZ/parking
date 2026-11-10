package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付订单
 */
@Data
@TableName("pay_order")
public class PayOrder {

    public static final String BIZ_SUBSCRIPTION = "SUBSCRIPTION";
    public static final String BIZ_TEMP_PARKING = "TEMP_PARKING";

    public static final String STATUS_CREATED = "CREATED"; // 已创建，未调起支付
    public static final String STATUS_PENDING = "PENDING"; // 已下单待支付
    public static final String STATUS_PAID    = "PAID";    // 已支付
    public static final String STATUS_CLOSED  = "CLOSED";  // 已关闭
    public static final String STATUS_REFUNDED = "REFUNDED"; // 已退款

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商户订单号 */
    private String orderNo;

    /** 付款用户：wx_user.id */
    private Long userId;

    /** 业务类型：SUBSCRIPTION/TEMP_PARKING */
    private String bizType;

    /** 关联业务ID（SUBSCRIPTION=vehicleId / TEMP_PARKING=记录id） */
    private Long bizId;

    /** 套餐类型（bizType=SUBSCRIPTION 时） */
    private String planType;

    /** 支付金额（分） */
    private Integer amount;

    /** 状态 */
    private String status;

    private String prepayId;

    private String transactionId;

    private LocalDateTime paidTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
