package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("parking_lot")
public class ParkingLot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String location;

    private Integer totalSpaces;

    /** 最大可绑定车辆数，null 不限 */
    private Integer bindLimit;

    private BigDecimal monthlyPrice;

    private BigDecimal quarterlyPrice;

    private BigDecimal yearlyPrice;

    // 临停规则
    private Integer freeMinutes;

    private Integer firstHours;

    private BigDecimal firstPrice;

    private BigDecimal hourlyPrice;

    private BigDecimal dailyMax;

    private String qrcodeSecret;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
