package com.parking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("temp_parking_record")
public class TempParkingRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long lotId;

    private String plateNo;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    /** 应收费用（元） */
    private BigDecimal fee;

    /** 0未支付 1已支付 */
    private Integer payStatus;
}
