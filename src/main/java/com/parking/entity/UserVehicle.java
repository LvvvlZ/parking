package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_vehicle")
public class UserVehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long vehicleId;

    /** 是否车主 */
    private Integer isOwner;

    private LocalDateTime bindTime;
}
