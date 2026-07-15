<template>
  <view class="container">
    <view class="card">
      <view class="section-title">我的账户</view>
      <view v-if="loading" class="muted">加载中...</view>
      <view v-else-if="!profile" class="button-primary" @click="goLogin">登录</view>
      <view v-else class="menu-list">
        <view class="menu-item"><text>会员编号</text><text class="muted">{{ profile.memberNo }}</text></view>
        <view class="menu-item"><text>账号</text><text class="muted">{{ profile.username }}</text></view>
        <view class="menu-item"><text>手机号</text><text class="muted">{{ profile.mobile }}</text></view>
        <view class="menu-item"><text>会员等级</text><text class="muted">{{ profile.levelCode }}</text></view>
        <view class="menu-item"><text>状态</text><text class="muted">{{ profile.status }}</text></view>
        <view class="button-primary logout-button" @click="logout">退出登录</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { fetchProfile } from '../../api/member'
import { clearToken } from '../../utils/auth'

const loading = ref(false)
const profile = ref(null)

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function logout() {
  clearToken()
  profile.value = null
  uni.showToast({ title: '已退出', icon: 'success' })
}

async function loadProfile() {
  loading.value = true
  try {
    profile.value = await fetchProfile()
  } catch {
    profile.value = null
  } finally {
    loading.value = false
  }
}

loadProfile()
</script>

<style scoped>
.logout-button {
  margin-top: 24rpx;
}
</style>
