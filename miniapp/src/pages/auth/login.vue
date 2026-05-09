<template>
  <view class="page">
    <view class="card">
      <text class="section-title">手机号登录</text>
      <text class="section-subtitle">如需把微信身份绑定到已有会员，请先填写手机号，再使用微信登录。</text>
    </view>

    <view class="card form-card">
      <view class="field-stack">
        <view>
          <text class="field-label">手机号</text>
          <input v-model.trim="form.phone" class="input" type="number" maxlength="11" placeholder="请输入 11 位手机号" />
        </view>
        <view>
          <text class="field-label">密码</text>
          <input v-model.trim="form.password" class="input" password placeholder="请输入密码" />
        </view>
      </view>

      <view class="actions top-gap">
        <button class="button-primary" :loading="submitting" @click="submitLogin">登录</button>
        <button class="button-secondary" :loading="wechatLoading" @click="submitWechatLogin">微信登录</button>
        <button plain @click="goToRegister">没有账号，去注册</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useMemberStore } from '@/stores/member'

const memberStore = useMemberStore()
const submitting = ref(false)
const wechatLoading = ref(false)

const form = reactive({
  phone: '',
  password: '',
})

async function submitLogin() {
  if (!form.phone || !form.password) {
    uni.showToast({
      title: '请先填写手机号和密码',
      icon: 'none',
    })
    return
  }

  submitting.value = true
  try {
    await memberStore.login({
      phone: form.phone,
      password: form.password,
    })
    uni.switchTab({
      url: '/pages/index/index',
    })
  } finally {
    submitting.value = false
  }
}

async function submitWechatLogin() {
  wechatLoading.value = true
  try {
    const result = await uni.login()
    if (!result.code) {
      throw new Error('微信登录 code 获取失败')
    }
    await memberStore.wechatLogin({
      code: result.code,
      phone: form.phone || undefined,
    })
    uni.switchTab({
      url: '/pages/index/index',
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '微信登录失败'
    uni.showToast({
      title: message,
      icon: 'none',
    })
  } finally {
    wechatLoading.value = false
  }
}

function goToRegister() {
  uni.navigateTo({
    url: '/pages/auth/register',
  })
}
</script>

<style scoped lang="scss">
.form-card {
  margin-top: 24rpx;
}

.top-gap {
  margin-top: 28rpx;
}

.input {
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  border: 2rpx solid #ddd2c2;
  background: #fff;
}
</style>
