<template>
  <view class="page">
    <!-- 用户信息 -->
    <view class="user-card">
      <view class="avatar">{{ (userStore.nickname || '客')[0] }}</view>
      <view class="user-info">
        <text class="nickname">{{ userStore.nickname || '未登录' }}</text>
        <text class="uid" v-if="userStore.isLoggedIn">ID: {{ userStore.userId }}</text>
      </view>
    </view>

    <!-- 菜单 -->
    <view class="menu-card">
      <view class="menu-item" @click="goSubscriptions">
        <text class="menu-icon">📋</text>
        <text class="menu-text">我的订阅记录</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goVehicles">
        <text class="menu-icon">🚗</text>
        <text class="menu-text">我的车辆</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="refreshProfile">
        <text class="menu-icon">🔄</text>
        <text class="menu-text">刷新资料</text>
        <text class="menu-arrow">></text>
      </view>
    </view>

    <button v-if="userStore.isLoggedIn" class="btn-logout" @click="confirmLogout">退出登录</button>
    <button v-else class="btn-login" @click="goLogin">去登录</button>
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

function goSubscriptions() {
  if (!requireLogin()) return
  uni.navigateTo({ url: '/pages/subscriptions/subscriptions' })
}

function goVehicles() {
  uni.switchTab({ url: '/pages/vehicles/vehicles' })
}

async function refreshProfile() {
  if (!requireLogin()) return
  await userStore.fetchProfile()
  uni.showToast({ title: '已刷新', icon: 'success' })
}

function confirmLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定退出当前账号吗？',
    success: (res) => {
      if (res.confirm) userStore.logout()
    }
  })
}

function goLogin() {
  uni.reLaunch({ url: '/pages/login/login' })
}

function requireLogin(): boolean {
  if (!userStore.isLoggedIn) {
    uni.reLaunch({ url: '/pages/login/login' })
    return false
  }
  return true
}
</script>

<style lang="scss" scoped>
.page {
  padding: 30rpx;
}

.user-card {
  background: linear-gradient(135deg, #1296db 0%, #0e7cc2 100%);
  border-radius: 32rpx;
  padding: 50rpx 40rpx;
  display: flex;
  align-items: center;
  gap: 30rpx;
  .avatar {
    width: 110rpx;
    height: 110rpx;
    background: rgba(255, 255, 255, 0.25);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 48rpx;
    color: #fff;
    font-weight: bold;
  }
  .user-info {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    .nickname {
      font-size: 36rpx;
      font-weight: bold;
      color: #fff;
    }
    .uid {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

.menu-card {
  margin-top: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  .menu-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 32rpx 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    &:last-child { border-bottom: none; }
    .menu-icon { font-size: 36rpx; }
    .menu-text {
      flex: 1;
      font-size: 30rpx;
      color: #333;
    }
    .menu-arrow {
      color: #c0c4cc;
      font-size: 28rpx;
    }
  }
}

.btn-logout {
  margin-top: 60rpx;
  width: 100%;
  height: 92rpx;
  background: #fff;
  color: #fa3534;
  font-size: 30rpx;
  border-radius: 46rpx;
  border: none;
  &::after { border: none; }
}

.btn-login {
  margin-top: 60rpx;
  width: 100%;
  height: 92rpx;
  background: #07c160;
  color: #fff;
  font-size: 30rpx;
  border-radius: 46rpx;
  border: none;
  &::after { border: none; }
}
</style>
