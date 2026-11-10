package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_lot")
public class AdminLot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adminId;

    private Long lotId;

    private LocalDateTime createTime;
}
