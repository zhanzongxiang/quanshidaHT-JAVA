<template>
  <view class="page">
    <view class="card">
      <text class="section-title">会员注册</text>
      <text class="section-subtitle">注册完成后会自动登录，并可直接查看会员资料与可见运单。</text>
    </view>

    <view class="card form-card">
      <view class="field-stack">
        <view>
          <text class="field-label">手机号</text>
          <input v-model.trim="form.phone" class="input" type="number" maxlength="11" placeholder="请输入手机号" />
        </view>
        <view>
          <text class="field-label">密码</text>
          <input v-model.trim="form.password" class="input" password placeholder="请输入密码" />
        </view>
        <view>
          <text class="field-label">昵称</text>
          <input v-model.trim="form.nickname" class="input" placeholder="可选" />
        </view>
        <view>
          <text class="field-label">姓名</text>
          <input v-model.trim="form.fullName" class="input" placeholder="可选" />
        </view>
      </view>

      <view class="actions top-gap">
        <button class="button-primary" :loading="submitting" @click="submitRegister">注册并登录</button>
        <button plain @click="goToLogin">已有账号，去登录</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useMemberStore } from '@/stores/member'

const memberStore = useMemberStore()
const submitting = ref(false)
const redirectUrl = ref('/pages/index/index')

const form = reactive({
  phone: '',
  password: '',
  nickname: '',
  fullName: '',
})

onLoad((query) => {
  redirectUrl.value = typeof query?.redirect === 'string' && query.redirect ? query.redirect : '/pages/index/index'
})

async function submitRegister() {
  if (!form.phone || !form.password) {
    uni.showToast({
      title: '手机号和密码必填',
      icon: 'none',
    })
    return
  }

  submitting.value = true
  try {
    await memberStore.register({
      phone: form.phone,
      password: form.password,
      nickname: form.nickname,
      fullName: form.fullName,
    })
    navigateAfterAuth()
  } finally {
    submitting.value = false
  }
}

function goToLogin() {
  uni.redirectTo({
    url: `/pages/auth/login?redirect=${encodeURIComponent(redirectUrl.value)}`,
  })
}

function navigateAfterAuth() {
  if (redirectUrl.value.startsWith('/pages/index/index')) {
    uni.switchTab({
      url: '/pages/index/index',
    })
    return
  }
  if (redirectUrl.value.startsWith('/pages/waybill/list')) {
    uni.switchTab({
      url: '/pages/waybill/list',
    })
    return
  }
  if (redirectUrl.value.startsWith('/pages/payment/list')) {
    uni.switchTab({
      url: '/pages/payment/list',
    })
    return
  }
  if (redirectUrl.value.startsWith('/pages/profile/index')) {
    uni.switchTab({
      url: '/pages/profile/index',
    })
    return
  }
  uni.redirectTo({
    url: redirectUrl.value,
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
