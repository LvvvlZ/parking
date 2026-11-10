package com.parking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminAssignReq {

    @NotNull(message = "管理员ID不能为空")
    private Long adminId;

    @NotEmpty(message = "车场ID不能为空")
    private List<Long> lotIds;
}
