<template>
  <view class="page">
    <!-- 车场信息 -->
    <view class="lot-card" v-if="lot">
      <text class="lot-name">{{ lot.name }}</text>
      <text class="lot-location">📍 {{ lot.location }}</text>
      <view class="lot-stats">
        <view class="stat">
          <text class="stat-num">{{ lot.totalSpaces }}</text>
          <text class="stat-label">车位数</text>
        </view>
        <view class="stat">
          <text class="stat-num">{{ lot.bindCount }}</text>
          <text class="stat-label">已绑定</text>
        </view>
        <view class="stat">
          <text class="stat-num">{{ lot.freeMinutes }}</text>
          <text class="stat-label">临停免费(分)</text>
        </view>
      </view>
      <view class="price-row">
        <text class="price" v-if="lot.monthlyPrice">月付 ¥{{ lot.monthlyPrice }}</text>
        <text class="price" v-if="lot.quarterlyPrice">季付 ¥{{ lot.quarterlyPrice }}</text>
        <text class="price" v-if="lot.yearlyPrice">年付 ¥{{ lot.yearlyPrice }}</text>
      </view>
    </view>

    <!-- 添加车辆 -->
    <view class="form-card">
      <text class="form-title">添加车辆</text>
      <view class="input-row">
        <input
          v-model="plateNo"
          class="input"
          placeholder="请输入车牌号，如 粤B12345"
          maxlength="8"
        />
      </view>
      <button class="btn-submit" :loading="submitting" @click="submit">
        绑定到该车场
      </button>
      <text class="tip">同一辆车可被多人绑定共用；绑定后可在“我的车辆”中续费套餐</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { lotApi, vehicleApi } from '@/api'
import type { LotVO } from '@/types'

const lot = ref<LotVO | null>(null)
const lotId = ref('')
const sign = ref('')
const plateNo = ref('')
const submitting = ref(false)

onLoad(async (query) => {
  lotId.value = (query?.lotId as string) || ''
  sign.value = (query?.sign as string) || ''
  if (!lotId.value || !sign.value) {
    uni.showToast({ title: '二维码参数缺失', icon: 'none' })
    return
  }
  try {
    const res = await lotApi.scan(lotId.value, sign.value)
    lot.value = res.data
  } catch (e) {}
})

async function submit() {
  const plate = plateNo.value.trim().toUpperCase()
  if (!plate) {
    uni.showToast({ title: '请输入车牌号', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await vehicleApi.add({ plateNo: plate, lotId: lotId.value, sign: sign.value })
    uni.showToast({ title: '绑定成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/vehicles/vehicles' })
    }, 800)
  } catch (e) {
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page {
  padding: 30rpx;
}

.lot-card {
  background: linear-gradient(135deg, #1296db 0%, #0e7cc2 100%);
  border-radius: 32rpx;
  padding: 40rpx;
  color: #fff;
  .lot-name {
    font-size: 40rpx;
    font-weight: bold;
  }
  .lot-location {
    font-size: 26rpx;
    opacity: 0.85;
    margin-top: 12rpx;
    display: block;
  }
  .lot-stats {
    display: flex;
    justify-content: space-around;
    margin-top: 36rpx;
    .stat {
      display: flex;
      flex-direction: column;
      align-items: center;
      .stat-num {
        font-size: 36rpx;
        font-weight: bold;
      }
      .stat-label {
        font-size: 22rpx;
        opacity: 0.8;
        margin-top: 8rpx;
      }
    }
  }
  .price-row {
    display: flex;
    gap: 20rpx;
    margin-top: 32rpx;
    .price {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 24rpx;
      padding: 8rpx 24rpx;
      font-size: 24rpx;
    }
  }
}

.form-card {
  margin-top: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  .form-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
  }
  .input-row {
    margin-top: 30rpx;
    .input {
      height: 96rpx;
      background: #f8f8f8;
      border-radius: 16rpx;
      padding: 0 30rpx;
      font-size: 30rpx;
    }
  }
  .btn-submit {
    margin-top: 40rpx;
    width: 100%;
    height: 96rpx;
    background: #1296db;
    color: #fff;
    border-radius: 48rpx;
    font-size: 32rpx;
    font-weight: 500;
    &::after {
      border: none;
    }
  }
  .tip {
    display: block;
    margin-top: 20rpx;
    font-size: 22rpx;
    color: #999;
    line-height: 1.6;
  }
}
</style>
