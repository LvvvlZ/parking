<template>
  <view class="page">
    <view v-if="vehicles.length" class="vehicle-list">
      <view v-for="v in vehicles" :key="v.id" class="vehicle-card">
        <view class="card-top">
          <view class="plate">{{ v.plateNo }}</view>
          <view class="status-tag" :class="v.subStatus">
            {{ v.subStatus === 'VALID' ? '套餐有效' : v.subStatus === 'EXPIRED' ? '已到期' : '未订阅' }}
          </view>
        </view>
        <text class="lot-line">🅿️ {{ v.lotName }}</text>
        <text class="expire-line">
          {{ v.subStatus === 'NONE' ? '尚未订阅套餐' : (v.subStatus === 'VALID' ? '到期时间：' : '已于 ') + (v.latestExpire || '').replace('T', ' ').slice(0, 16) }}
        </text>
        <text class="owner-line" v-if="v.isOwner !== null">{{ v.isOwner ? '我是车主' : '授权使用人' }}</text>
        <view class="btn-row">
          <button class="btn renew" @click="choosePlan(v)">
            {{ v.subStatus === 'VALID' ? '续费（到期顺延）' : '订阅套餐' }}
          </button>
          <button class="btn records" @click="goRecords(v.id)">订阅记录</button>
          <button class="btn unbind" @click="confirmUnbind(v)">解绑</button>
        </view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-icon">🚙</text>
      <text class="empty-text">暂无绑定车辆</text>
      <button class="btn-scan" @click="goScan">去扫码添加</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { vehicleApi, subscriptionApi } from '@/api'
import { useUserStore } from '@/store/user'
import { PLAN_TEXT } from '@/types'
import type { VehicleVO, PlanType } from '@/types'

const userStore = useUserStore()
const vehicles = ref<VehicleVO[]>([])

onShow(() => {
  if (userStore.isLoggedIn) {
    load()
  } else {
    uni.reLaunch({ url: '/pages/login/login' })
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

// 选择套餐并拉起微信支付
function choosePlan(v: VehicleVO) {
  const plans: PlanType[] = ['MONTH', 'QUARTER', 'YEAR']
  uni.showActionSheet({
    itemList: ['包月', '包季', '包年'],
    success: async (res) => {
      const planType = plans[res.tapIndex]
      try {
        const orderRes = await subscriptionApi.order(v.id, planType)
        const p = orderRes.data
        // 模拟支付：后端 mock 开关开启时 packageVal 为 "mock:orderId"
        if (p.packageVal && p.packageVal.startsWith('mock:')) {
          await subscriptionApi.mockNotify(Number(p.packageVal.slice(5)))
          uni.showToast({ title: '支付成功(模拟)', icon: 'success' })
          setTimeout(load, 800)
          return
        }
        uni.requestPayment({
          provider: 'wxpay',
          timeStamp: p.timeStamp,
          nonceStr: p.nonceStr,
          package: p.packageVal,
          signType: (p.signType || 'RSA') as any,
          paySign: p.paySign,
          success: () => {
            uni.showToast({ title: '支付成功', icon: 'success' })
            setTimeout(load, 800)
          },
          fail: (err) => {
            console.log('支付取消或失败', err)
            uni.showToast({ title: '未完成支付', icon: 'none' })
          }
        })
      } catch (e) {}
    }
  })
}

function goRecords(vehicleId: number) {
  uni.navigateTo({ url: `/pages/subscriptions/subscriptions?vehicleId=${vehicleId}` })
}

function confirmUnbind(v: VehicleVO) {
  uni.showModal({
    title: '解绑确认',
    content: `确定解绑车辆 ${v.plateNo} 吗？`,
    success: async (res) => {
      if (res.confirm) {
        await vehicleApi.unbind(v.id)
        uni.showToast({ title: '已解绑', icon: 'success' })
        await load()
      }
    }
  })
}

function goScan() {
  uni.switchTab({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.page {
  padding: 30rpx;
}

.vehicle-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  .card-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .plate {
      background: #2b5a87;
      color: #fff;
      font-size: 32rpx;
      font-weight: bold;
      padding: 12rpx 24rpx;
      border-radius: 8rpx;
      letter-spacing: 2rpx;
    }
    .status-tag {
      font-size: 24rpx;
      padding: 6rpx 20rpx;
      border-radius: 24rpx;
      &.VALID { background: #e8f9ef; color: #07c160; }
      &.EXPIRED { background: #fdeaea; color: #fa3534; }
      &.NONE { background: #f0f0f0; color: #999; }
    }
  }
  .lot-line {
    display: block;
    margin-top: 20rpx;
    font-size: 28rpx;
    color: #333;
  }
  .expire-line {
    display: block;
    margin-top: 10rpx;
    font-size: 26rpx;
    color: #666;
  }
  .owner-line {
    display: block;
    margin-top: 6rpx;
    font-size: 24rpx;
    color: #999;
  }
  .btn-row {
    display: flex;
    gap: 16rpx;
    margin-top: 24rpx;
    .btn {
      flex: 1;
      height: 76rpx;
      font-size: 26rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12rpx;
      padding: 0;
      &::after { border: none; }
      &.renew { background: #1296db; color: #fff; }
      &.records { background: #f0f7ff; color: #1296db; }
      &.unbind { background: #fff5f5; color: #fa3534; }
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
  .btn-scan {
    margin-top: 40rpx;
    background: #1296db;
    color: #fff;
    font-size: 28rpx;
    border-radius: 44rpx;
    padding: 0 60rpx;
    height: 88rpx;
    display: flex;
    align-items: center;
    &::after { border: none; }
  }
}
</style>
