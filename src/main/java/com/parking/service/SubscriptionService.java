package com.parking.service;

import com.parking.dto.SubscriptionVO;

import java.util.List;

public interface SubscriptionService {

    /** 我的订阅记录（我付费的所有记录） */
    List<SubscriptionVO> mySubscriptions();

    /** 某车辆订阅记录（需为绑定人） */
    List<SubscriptionVO> vehicleSubscriptions(Long vehicleId);
}
