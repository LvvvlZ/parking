package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 道闸/车牌识别回调请求（车场签名鉴权）
 */
@Data
public class TempGateReq {

    @NotNull(message = "车场ID不能为空")
    private Long lotId;

    @NotBlank(message = "签名不能为空")
    private String sign;

    @NotBlank(message = "车牌号不能为空")
    private String plateNo;
}
