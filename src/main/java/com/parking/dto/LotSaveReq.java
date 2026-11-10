package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LotSaveReq {

    @NotBlank(message = "车场名称不能为空")
    private String name;

    @NotBlank(message = "位置不能为空")
    private String location;

    @NotNull(message = "车位数不能为空")
    private Integer totalSpaces;

    /** 最大可绑定车辆数，null 不限 */
    private Integer bindLimit;

    private BigDecimal monthlyPrice;

    private BigDecimal quarterlyPrice;

    private BigDecimal yearlyPrice;

    /** 临停规则 */
    private Integer freeMinutes;

    private Integer firstHours;

    private BigDecimal firstPrice;

    private BigDecimal hourlyPrice;

    private BigDecimal dailyMax;
}
