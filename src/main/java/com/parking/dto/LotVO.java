package com.parking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LotVO {

    private Long id;

    private String name;

    private String location;

    private Integer totalSpaces;

    private Integer bindLimit;

    /** 当前绑定车辆数 */
    private Integer bindCount;

    private BigDecimal monthlyPrice;

    private BigDecimal quarterlyPrice;

    private BigDecimal yearlyPrice;

    private Integer freeMinutes;

    private Integer firstHours;

    private BigDecimal firstPrice;

    private BigDecimal hourlyPrice;

    private BigDecimal dailyMax;

    private LocalDateTime createTime;
}
