<template>
  <view class="login-page">
    <view class="bg-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
    </view>

    <view class="logo-section">
      <view class="logo-box">
        <text class="logo-icon">🚗</text>
      </view>
      <text class="app-name">停车管理</text>
      <text class="app-slogan">扫码绑车，套餐续费更省心</text>
    </view>

    <view class="login-section">
      <button class="btn-wechat" :loading="loading" @click="handleLogin" hover-class="btn-hover">
        <text class="btn-text">微信一键登录</text>
      </button>
      <view class="agreement">
        <text class="agreement-text">登录即表示同意</text>
        <text class="link">《用户协议》</text>
        <text class="agreement-text">和</text>
        <text class="link">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    const loginRes: any = await uni.login({ provider: 'weixin' })
    if (!loginRes || !loginRes.code) {
      uni.showToast({ title: '获取登录凭证失败', icon: 'none' })
      return
    }
    const success = await userStore.login(loginRes.code)
    if (success) {
      uni.showToast({ title: '登录成功', icon: 'success' })
      setTimeout(() => {
        uni.switchTab({ url: '/pages/index/index' })
      }, 800)
    }
  } catch (e) {
    console.error('登录错误:', e)
    uni.showToast({ title: '登录失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f4fd 0%, #f8f8f8 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 60rpx;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  .circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
  }
  .circle-1 {
    width: 600rpx;
    height: 600rpx;
    background: #1296db;
    top: -200rpx;
    right: -200rpx;
  }
  .circle-2 {
    width: 400rpx;
    height: 400rpx;
    background: #07c160;
    bottom: -100rpx;
    left: -100rpx;
  }
}

.logo-section {
  margin-top: 180rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  .logo-box {
    width: 160rpx;
    height: 160rpx;
    background: linear-gradient(135deg, #1296db 0%, #0e7cc2 100%);
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 16rpx 40rpx rgba(18, 150, 219, 0.3);
    .logo-icon {
      font-size: 80rpx;
    }
  }
  .app-name {
    font-size: 52rpx;
    font-weight: bold;
    color: #333;
    margin-top: 30rpx;
  }
  .app-slogan {
    font-size: 28rpx;
    color: #999;
    margin-top: 12rpx;
  }
}

.login-section {
  margin-top: auto;
  margin-bottom: 120rpx;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.btn-wechat {
  width: 100%;
  height: 96rpx;
  background: #07c160;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  .btn-text {
    font-size: 32rpx;
    color: #fff;
    font-weight: 500;
  }
  &::after {
    border: none;
  }
}

.agreement {
  display: flex;
  align-items: center;
  gap: 8rpx;
  .agreement-text {
    font-size: 24rpx;
    color: #999;
  }
  .link {
    font-size: 24rpx;
    color: #1296db;
  }
}
</style>
