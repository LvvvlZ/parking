package com.parking.service;

import com.parking.dto.LotVO;

import java.util.List;

public interface ParkingLotService {

    /**
     * 当前管理员可见的车场：SUPER_ADMIN 全部，ADMIN 仅分配的
     */
    List<LotVO> myLots();

    LotVO detail(Long lotId);

    /**
     * 用户端扫码后查看车场信息（免管理员权限）
     */
    LotVO publicDetail(Long lotId);

    /**
     * 校验当前管理员对车场的管理权限（ADMIN 需已分配，SUPER_ADMIN 直接过）
     */
    void checkLotPermission(Long lotId);

    /**
     * 生成车场二维码内容（lotId + 防伪签名）
     */
    String qrcodePayload(Long lotId);

    /**
     * 校验扫码内容（用户端公开接口使用）
     */
    void verifyScan(Long lotId, String sign);
}
