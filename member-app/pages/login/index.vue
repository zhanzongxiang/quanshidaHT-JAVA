<template>
  <view class="container">
    <view class="card">
      <view class="section-title">会员登录</view>
      <view class="form">
        <input v-model="form.account" class="input" placeholder="账号或手机号" />
        <input v-model="form.password" class="input" password placeholder="密码" />
        <view class="button-primary" @click="submitLogin">登录</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive } from 'vue'
import { login } from '../../api/member'
import { setToken } from '../../utils/auth'

const form = reactive({
  account: '',
  password: '',
})

async function submitLogin() {
  if (!form.account.trim() || !form.password.trim()) {
    uni.showToast({ title: '请输入账号和密码', icon: 'none' })
    return
  }
  try {
    const result = await login({
      account: form.account.trim(),
      password: form.password,
    })
    setToken(result.accessToken, result.tokenType)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' })
    }, 300)
  } catch (error) {
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  }
}
</script>

<style scoped>
.form {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.input {
  box-sizing: border-box;
  border-radius: 18rpx;
  background: #f9fafb;
  color: #111827;
  font-size: 28rpx;
  height: 88rpx;
  padding: 0 22rpx;
  width: 100%;
}
</style>
