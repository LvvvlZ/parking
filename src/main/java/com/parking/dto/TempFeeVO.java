package com.parking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TempFeeVO {

    private Long recordId;

    private String plateNo;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    /** 停车时长（分钟） */
    private Long minutes;

    private BigDecimal fee;

    /** true=在免费时长内无需缴费 */
    private Boolean free;
}
