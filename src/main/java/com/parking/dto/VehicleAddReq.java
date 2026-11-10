package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleAddReq {

    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    /** 扫码获得的车场ID */
    @NotNull(message = "车场ID不能为空")
    private Long lotId;

    /** 扫码获得的防伪签名 */
    @NotBlank(message = "签名不能为空")
    private String sign;
}
