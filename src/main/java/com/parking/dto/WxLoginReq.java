package com.parking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginReq {

    /** wx.login 获取的 code */
    @NotBlank(message = "code不能为空")
    private String code;

    private String nickname;

    private String avatar;
}
