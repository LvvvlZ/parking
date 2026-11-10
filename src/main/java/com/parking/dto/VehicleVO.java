package com.parking.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VehicleVO {

    private Long id;

    private String plateNo;

    private Long lotId;

    private String lotName;

    /** 当前用户是否车主 */
    private Boolean isOwner;

    /** 最新套餐到期时间，null 表示未订阅 */
    private LocalDateTime latestExpire;

    private String planType;

    /** 套餐状态：VALID有效 / EXPIRED已到期 / NONE未订阅 */
    private String subStatus;

    /** 所有绑定用户ID（车主在前） */
    private List<Long> userIds;
}
