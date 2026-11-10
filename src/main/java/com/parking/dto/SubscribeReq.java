package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscribeReq {

    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;

    /** MONTH/QUARTER/YEAR */
    @NotBlank(message = "套餐类型不能为空")
    private String planType;
}
