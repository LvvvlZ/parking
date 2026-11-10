<template>
  <view class="page">
    <view v-if="records.length" class="record-list">
      <view v-for="r in records" :key="r.id" class="record-card">
        <view class="record-top">
          <text class="plate">{{ r.plateNo }}</text>
          <text class="plan">{{ PLAN_TEXT[r.planType] }}</text>
        </view>
        <view class="record-row">
          <text class="label">成交价</text>
          <text class="value price">¥{{ r.price }}</text>
        </view>
        <view class="record-row">
          <text class="label">开始时间</text>
          <text class="value">{{ fmt(r.startTime) }}</text>
        </view>
        <view class="record-row">
          <text class="label">到期时间</text>
          <text class="value" :class="{ expired: isExpired(r) }">{{ fmt(r.expireTime) }}</text>
        </view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无订阅记录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { subscriptionApi } from '@/api'
import { PLAN_TEXT } from '@/types'
import type { SubscriptionVO } from '@/types'

const records = ref<SubscriptionVO[]>([])
const vehicleId = ref<number | null>(null)

onLoad(async (query) => {
  vehicleId.value = query?.vehicleId ? Number(query.vehicleId) : null
  await load()
})

onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})

async function load() {
  try {
    const res = vehicleId.value
      ? await subscriptionApi.byVehicle(vehicleId.value)
      : await subscriptionApi.my()
    records.value = res.data || []
  } catch (e) {}
}

function fmt(t: string): string {
  return t ? t.replace('T', ' ').slice(0, 16) : '-'
}

function isExpired(r: SubscriptionVO): boolean {
  return new Date(r.expireTime.replace('T', ' ').replace(/-/g, '/')).getTime() < Date.now()
}
</script>

<style lang="scss" scoped>
.page {
  padding: 30rpx;
}

.record-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  .record-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 20rpx;
    border-bottom: 1rpx solid #f0f0f0;
    .plate {
      background: #2b5a87;
      color: #fff;
      font-size: 28rpx;
      font-weight: bold;
      padding: 8rpx 20rpx;
      border-radius: 8rpx;
      letter-spacing: 2rpx;
    }
    .plan {
      font-size: 28rpx;
      font-weight: bold;
      color: #1296db;
    }
  }
  .record-row {
    display: flex;
    justify-content: space-between;
    margin-top: 16rpx;
    .label {
      font-size: 26rpx;
      color: #999;
    }
    .value {
      font-size: 26rpx;
      color: #333;
      &.price { color: #fa5511; font-weight: bold; }
      &.expired { color: #fa3534; }
    }
  }
}

.empty {
  padding-top: 200rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  .empty-icon { font-size: 100rpx; }
  .empty-text {
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #999;
  }
}
</style>
