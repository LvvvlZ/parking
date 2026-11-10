<template>
  <view class="page">
    <!-- 扫码入口 -->
    <view class="scan-card" @click="handleScan">
      <text class="scan-icon">📷</text>
      <text class="scan-title">扫码绑定车场</text>
      <text class="scan-desc">扫描车场二维码，添加您的车辆</text>
    </view>

    <!-- 我的车辆预览 -->
    <view class="section">
      <view class="section-head">
        <text class="section-title">我的车辆</text>
        <text class="section-more" @click="goVehicles">全部 ></text>
      </view>
      <view v-if="vehicles.length" class="vehicle-list">
        <view v-for="v in vehicles" :key="v.id" class="vehicle-item" @click="goVehicles">
          <view class="plate">{{ v.plateNo }}</view>
          <view class="vehicle-info">
            <text class="lot-name">{{ v.lotName }}</text>
            <text class="expire" :class="subClass(v.subStatus)">
              {{ subText(v) }}
            </text>
          </view>
        </view>
      </view>
      <view v-else class="empty">
        <text class="empty-text">还没有绑定车辆，先扫码添加吧</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { vehicleApi } from '@/api'
import { useUserStore } from '@/store/user'
import { PLAN_TEXT } from '@/types'
import type { VehicleVO } from '@/types'

const userStore = useUserStore()
const vehicles = ref<VehicleVO[]>([])

onShow(() => {
  if (userStore.isLoggedIn) {
    load()
  }
})

onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})

async function load() {
  try {
    const res = await vehicleApi.my()
    vehicles.value = res.data || []
  } catch (e) {}
}

function subText(v: VehicleVO): string {
  if (v.subStatus === 'NONE') return '未订阅'
  const date = v.latestExpire ? v.latestExpire.slice(0, 10) : ''
  return `${PLAN_TEXT[v.planType as keyof typeof PLAN_TEXT] || ''} ${v.subStatus === 'VALID' ? '至' : '已于'} ${date}`
}

function subClass(status: string): string {
  return status === 'VALID' ? 'valid' : status === 'EXPIRED' ? 'expired' : 'none'
}

function handleScan() {
  if (!userStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  uni.scanCode({
    success: (res) => {
      // 二维码内容格式：lotId:sign
      const content = res.result || ''
      const idx = content.indexOf(':')
      if (idx <= 0) {
        uni.showToast({ title: '无效的车场二维码', icon: 'none' })
        return
      }
      const lotId = content.slice(0, idx)
      const sign = content.slice(idx + 1)
      uni.navigateTo({ url: `/pages/bind/bind?lotId=${lotId}&sign=${sign}` })
    },
    fail: () => {}
  })
}

function goVehicles() {
  uni.switchTab({ url: '/pages/vehicles/vehicles' })
}
</script>

<style lang="scss" scoped>
.page {
  padding: 30rpx;
}

.scan-card {
  background: linear-gradient(135deg, #1296db 0%, #0e7cc2 100%);
  border-radius: 32rpx;
  padding: 70rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 16rpx 40rpx rgba(18, 150, 219, 0.3);
  .scan-icon {
    font-size: 90rpx;
  }
  .scan-title {
    font-size: 40rpx;
    font-weight: bold;
    color: #fff;
    margin-top: 24rpx;
  }
  .scan-desc {
    font-size: 26rpx;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 12rpx;
  }
}

.section {
  margin-top: 40rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  .section-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
    .section-more {
      font-size: 26rpx;
      color: #1296db;
    }
  }
}

.vehicle-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  &:last-child {
    border-bottom: none;
  }
  .plate {
    background: #2b5a87;
    color: #fff;
    font-size: 28rpx;
    font-weight: bold;
    padding: 10rpx 20rpx;
    border-radius: 8rpx;
    letter-spacing: 2rpx;
  }
  .vehicle-info {
    display: flex;
    flex-direction: column;
    gap: 6rpx;
    .lot-name {
      font-size: 28rpx;
      color: #333;
    }
    .expire {
      font-size: 24rpx;
      &.valid { color: #07c160; }
      &.expired { color: #fa3534; }
      &.none { color: #999; }
    }
  }
}

.empty {
  padding: 40rpx 0;
  display: flex;
  justify-content: center;
  .empty-text {
    font-size: 26rpx;
    color: #999;
  }
}
</style>
