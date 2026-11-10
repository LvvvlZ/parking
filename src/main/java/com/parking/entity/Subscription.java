package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("subscription")
public class Subscription {

    public static final String PLAN_MONTH = "MONTH";
    public static final String PLAN_QUARTER = "QUARTER";
    public static final String PLAN_YEAR = "YEAR";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;

    private Long lotId;

    /** 付费用户：wx_user.id */
    private Long userId;

    /** 套餐：MONTH/QUARTER/YEAR */
    private String planType;

    /** 成交价（元） */
    private BigDecimal price;

    private LocalDateTime startTime;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;
}
