<template>
  <view class="page">
    <view class="card hero">
      <view class="pill">uni-app 小程序骨架</view>
      <text class="section-title">会员端联调入口</text>
      <text class="section-subtitle">
        当前骨架已对齐会员、运单、支付记录与支付准备接口，适合先打通后端链路，再逐步补完整产品体验。
      </text>
    </view>

    <view v-if="memberStore.profile" class="card profile-card">
      <text class="section-title">{{ memberStore.profile.nickname || memberStore.profile.fullName || memberStore.profile.phone }}</text>
      <text class="section-subtitle">手机号：{{ memberStore.profile.phone }}</text>
      <text class="section-subtitle">状态：{{ memberStore.profile.status }}</text>
      <text class="section-subtitle">
        微信绑定：{{ memberStore.isWechatBound ? `已绑定 ${memberStore.profile.wechatOpenid}` : '未绑定' }}
      </text>
    </view>

    <view v-else class="card">
      <text class="section-title">先完成登录</text>
      <text class="section-subtitle">支持手机号登录、注册，也预留了微信登录入口。</text>
      <view class="actions top-gap">
        <button class="button-primary" @click="goToLogin">手机号登录</button>
        <button class="button-secondary" @click="goToRegister">注册会员</button>
      </view>
    </view>

    <view class="grid-links">
      <view class="link-card" @click="openWaybills">
        <text class="link-title">我的运单</text>
        <text class="link-text">查看会员可见运单与物流节点</text>
      </view>
      <view class="link-card" @click="openPayments">
        <text class="link-title">支付记录</text>
        <text class="link-text">查看支付状态，联调支付结果</text>
      </view>
      <view class="link-card" @click="openProfile">
        <text class="link-title">我的资料</text>
        <text class="link-text">更新昵称、姓名、头像与微信状态</text>
      </view>
      <view class="link-card" @click="refreshProfile">
        <text class="link-title">刷新会话</text>
        <text class="link-text">重新拉取当前会员资料</text>
      </view>
    </view>

    <view class="card env-card">
      <text class="section-title">联调提示</text>
      <text class="section-subtitle">后端地址：{{ apiBaseUrl }}</text>
      <text class="section-subtitle">真实微信支付前请确认后台当前商户状态为 Ready。</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { API_BASE_URL } from '@/config/env'
import { useMemberStore } from '@/stores/member'

const memberStore = useMemberStore()
const apiBaseUrl = API_BASE_URL

onShow(async () => {
  memberStore.restore()
  if (memberStore.isAuthenticated) {
    try {
      await memberStore.fetchProfile()
    } catch {
      memberStore.logout(false)
    }
  }
})

function goToLogin() {
  uni.navigateTo({
    url: '/pages/auth/login',
  })
}

function goToRegister() {
  uni.navigateTo({
    url: '/pages/auth/register',
  })
}

function openWaybills() {
  uni.switchTab({
    url: '/pages/waybill/list',
  })
}

function openPayments() {
  uni.switchTab({
    url: '/pages/payment/list',
  })
}

function openProfile() {
  uni.switchTab({
    url: '/pages/profile/index',
  })
}

async function refreshProfile() {
  if (!memberStore.isAuthenticated) {
    goToLogin()
    return
  }
  await memberStore.fetchProfile()
  uni.showToast({
    title: '资料已刷新',
    icon: 'success',
  })
}
</script>

<style scoped lang="scss">
.hero,
.profile-card,
.env-card {
  margin-bottom: 24rpx;
}

.top-gap {
  margin-top: 28rpx;
}

.grid-links {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.link-card {
  min-height: 180rpx;
  padding: 28rpx 24rpx;
  background: linear-gradient(160deg, #fffdf8 0%, #efe4d2 100%);
  border: 2rpx solid #e5d7c4;
  border-radius: 28rpx;
}

.link-title {
  display: block;
  margin-bottom: 14rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #1d2f28;
}

.link-text {
  color: #6e675c;
  line-height: 1.6;
}
</style>
