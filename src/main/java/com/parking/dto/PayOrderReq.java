package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 支付下单请求：金额由服务端根据业务计算，不接受前端金额
 */
@Data
public class PayOrderReq {

    /** SUBSCRIPTION 套餐 / TEMP_PARKING 临停 */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /** SUBSCRIPTION -> vehicleId；TEMP_PARKING -> temp_parking_record.id */
    @NotNull(message = "业务ID不能为空")
    private Long bizId;

    /** 套餐类型（bizType=SUBSCRIPTION 时必填） */
    private String planType;
}
