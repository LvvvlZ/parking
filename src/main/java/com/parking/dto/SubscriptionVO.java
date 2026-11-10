package com.parking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionVO {

    private Long id;

    private Long vehicleId;

    private String plateNo;

    private String planType;

    private BigDecimal price;

    private LocalDateTime startTime;

    private LocalDateTime expireTime;
}
