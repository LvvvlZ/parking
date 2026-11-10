package com.parking.service;

import com.parking.dto.TempFeeVO;
import com.parking.dto.TempGateReq;
import com.parking.entity.TempParkingRecord;

public interface TempParkingService {

    /** 车辆入场（道闸回调） */
    TempParkingRecord entry(TempGateReq req);

    /** 车辆出场：计算费用并返回（道闸回调，之后用户通过支付接口缴费） */
    TempFeeVO exit(TempGateReq req);

    /**
     * 临停计费：免费时长 -> 首时段 -> 每小时 -> 单日封顶*天数
     */
    TempFeeVO calcFee(TempParkingRecord record);
}
