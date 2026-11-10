package com.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.common.BusinessException;
import com.parking.dto.TempFeeVO;
import com.parking.dto.TempGateReq;
import com.parking.entity.ParkingLot;
import com.parking.entity.TempParkingRecord;
import com.parking.mapper.ParkingLotMapper;
import com.parking.mapper.TempParkingRecordMapper;
import com.parking.service.ParkingLotService;
import com.parking.service.TempParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TempParkingServiceImpl implements TempParkingService {

    private final TempParkingRecordMapper tempParkingRecordMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final ParkingLotService parkingLotService;

    @Override
    public TempParkingRecord entry(TempGateReq req) {
        parkingLotService.verifyScan(req.getLotId(), req.getSign());
        List<TempParkingRecord> open = tempParkingRecordMapper.selectList(
                new LambdaQueryWrapper<TempParkingRecord>()
                        .eq(TempParkingRecord::getLotId, req.getLotId())
                        .eq(TempParkingRecord::getPlateNo, req.getPlateNo())
                        .isNull(TempParkingRecord::getExitTime));
        if (!open.isEmpty()) {
            throw new BusinessException("车辆已在场内");
        }
        TempParkingRecord record = new TempParkingRecord();
        record.setLotId(req.getLotId());
        record.setPlateNo(req.getPlateNo());
        record.setEntryTime(LocalDateTime.now());
        record.setPayStatus(0);
        tempParkingRecordMapper.insert(record);
        return record;
    }

    @Override
    public TempFeeVO exit(TempGateReq req) {
        parkingLotService.verifyScan(req.getLotId(), req.getSign());
        List<TempParkingRecord> open = tempParkingRecordMapper.selectList(
                new LambdaQueryWrapper<TempParkingRecord>()
                        .eq(TempParkingRecord::getLotId, req.getLotId())
                        .eq(TempParkingRecord::getPlateNo, req.getPlateNo())
                        .isNull(TempParkingRecord::getExitTime)
                        .orderByAsc(TempParkingRecord::getEntryTime));
        if (open.isEmpty()) {
            throw new BusinessException("无在场记录");
        }
        TempParkingRecord record = open.get(0);
        record.setExitTime(LocalDateTime.now());
        BigDecimal fee = calcFee(record).getFee();
        record.setFee(fee);
        tempParkingRecordMapper.updateById(record);
        return calcFee(record);
    }

    @Override
    public TempFeeVO calcFee(TempParkingRecord record) {
        ParkingLot lot = parkingLotMapper.selectById(record.getLotId());
        TempFeeVO vo = new TempFeeVO();
        vo.setRecordId(record.getId());
        vo.setPlateNo(record.getPlateNo());
        vo.setEntryTime(record.getEntryTime());
        vo.setExitTime(record.getExitTime());
        LocalDateTime end = record.getExitTime() == null ? LocalDateTime.now() : record.getExitTime();
        long minutes = Duration.between(record.getEntryTime(), end).toMinutes();
        vo.setMinutes(minutes);

        int freeMinutes = lot.getFreeMinutes() == null ? 0 : lot.getFreeMinutes();
        if (minutes <= freeMinutes) {
            vo.setFee(BigDecimal.ZERO);
            vo.setFree(true);
            return vo;
        }
        vo.setFree(false);
        int firstHours = lot.getFirstHours() == null ? 1 : lot.getFirstHours();
        BigDecimal fee = lot.getFirstPrice() == null ? BigDecimal.ZERO : lot.getFirstPrice();
        long billableMinutes = minutes - freeMinutes;
        long remainingMinutes = billableMinutes - firstHours * 60L;
        if (remainingMinutes > 0) {
            long extraHours = (remainingMinutes + 59) / 60;
            BigDecimal hourly = lot.getHourlyPrice() == null ? BigDecimal.ZERO : lot.getHourlyPrice();
            fee = fee.add(hourly.multiply(BigDecimal.valueOf(extraHours)));
        }
        // 单日封顶 * 停车天数
        if (lot.getDailyMax() != null) {
            long days = (minutes + 1439) / 1440;
            BigDecimal cap = lot.getDailyMax().multiply(BigDecimal.valueOf(days));
            if (fee.compareTo(cap) > 0) {
                fee = cap;
            }
        }
        vo.setFee(fee.setScale(2, RoundingMode.HALF_UP));
        return vo;
    }
}
