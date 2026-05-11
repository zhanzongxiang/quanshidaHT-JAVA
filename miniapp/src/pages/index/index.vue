<template>
  <view class="page">
    <view class="card hero">
      <view class="pill">uni-app 小程序骨架</view>
      <text class="section-title">会员端联调入口</text>
      <text class="section-subtitle">
        当前骨架已对齐会员、运单、支付记录和支付准备接口，适合先打通后端链路，再逐步补完整体产品体验。
      </text>
    </view>

    <view v-if="memberStore.profile" class="card profile-card">
      <view class="row-between">
        <text class="section-title">{{ displayName }}</text>
        <text class="pill">{{ memberStore.profile.status }}</text>
      </view>
      <text class="section-subtitle">手机号：{{ memberStore.profile.phone }}</text>
      <text class="section-subtitle">微信状态：{{ wechatBindStatus }}</text>
      <text v-if="memberStore.profile.wechatOpenid" class="section-subtitle">
        openid：{{ maskedWechatOpenid }}
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
        <text class="link-text">查看会员可见运单、进入详情并继续支付联调。</text>
      </view>
      <view class="link-card" @click="openPayments">
        <text class="link-title">支付记录</text>
        <text class="link-text">查看支付状态、回看结果页、校对支付同步情况。</text>
      </view>
      <view class="link-card" @click="openProfile">
        <text class="link-title">我的资料</text>
        <text class="link-text">维护昵称、姓名、头像，并查看微信绑定状态。</text>
      </view>
      <view class="link-card" @click="refreshProfile">
        <text class="link-title">刷新会话</text>
        <text class="link-text">重新拉取当前会员资料，验证登录态和资料回写。</text>
      </view>
      <view class="link-card" @click="openChecklist">
        <text class="link-title">联调工作台</text>
        <text class="link-text">按步骤记录登录、运单、支付和资料链路，并复制联调摘要。</text>
      </view>
      <view class="link-card" @click="openDiagnostics">
        <text class="link-title">环境诊断</text>
        <text class="link-text">快速查看后端地址、登录态、微信绑定和最近接口结果。</text>
      </view>
    </view>

    <view class="card env-card">
      <text class="section-title">联调提示</text>
      <text class="section-subtitle">后端地址：{{ apiBaseUrl }}</text>
      <text class="section-subtitle">真实微信支付前，请确认后台当前商户状态为 Ready。</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { API_BASE_URL } from '@/config/env'
import { useMemberStore } from '@/stores/member'
import { formatWechatBindStatus, maskIdentifier } from '@/utils/display'

const memberStore = useMemberStore()
const apiBaseUrl = API_BASE_URL

const displayName = computed(() => {
  const profile = memberStore.profile
  return profile?.nickname || profile?.fullName || profile?.phone || '会员'
})

const wechatBindStatus = computed(() => formatWechatBindStatus(memberStore.profile?.wechatOpenid))
const maskedWechatOpenid = computed(() => maskIdentifier(memberStore.profile?.wechatOpenid))

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

function openChecklist() {
  uni.navigateTo({
    url: '/pages/debug/checklist',
  })
}

function openDiagnostics() {
  uni.navigateTo({
    url: '/pages/debug/diagnostics',
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

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
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
