<template>
  <view class="page">
    <view class="card">
      <text class="section-title">手机号登录</text>
      <text class="section-subtitle">
        如需把微信身份绑定到已有会员，请先填写手机号，再使用微信登录。
      </text>
    </view>

    <view class="card form-card">
      <view class="field-stack">
        <view class="field-block">
          <text class="field-label">手机号</text>
          <input
            v-model="form.phone"
            class="input"
            type="number"
            maxlength="11"
            placeholder="请输入 11 位手机号"
            @input="handlePhoneInput"
          />
        </view>
        <view class="field-block">
          <text class="field-label">密码</text>
          <input
            v-model.trim="form.password"
            class="input"
            password
            placeholder="请输入至少 6 位密码"
          />
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
import { onLoad } from '@dcloudio/uni-app'
import { useMemberStore } from '@/stores/member'
import { navigateAfterAuth, openAppPage, resolveRedirectUrl } from '@/utils/navigation'
import { showError } from '@/utils/toast'
import { isValidPassword, isValidPhone, normalizePhone } from '@/utils/validation'
import { getWechatLoginCode } from '@/utils/wechat'

const memberStore = useMemberStore()
const submitting = ref(false)
const wechatLoading = ref(false)
const redirectUrl = ref('/pages/index/index')

const form = reactive({
  phone: '',
  password: '',
})

onLoad((query) => {
  redirectUrl.value = resolveRedirectUrl(typeof query?.redirect === 'string' ? query.redirect : undefined)
})

function handlePhoneInput(event: Event) {
  const value = (event as { detail?: { value?: string } }).detail?.value || ''
  form.phone = normalizePhone(value)
}

function validateCredentials() {
  if (!isValidPhone(form.phone)) {
    showError('请输入正确的 11 位手机号')
    return false
  }

  if (!isValidPassword(form.password)) {
    showError('密码至少需要 6 位')
    return false
  }

  return true
}

async function submitLogin() {
  if (!validateCredentials()) {
    return
  }

  submitting.value = true
  try {
    await memberStore.login({
      phone: normalizePhone(form.phone),
      password: form.password.trim(),
    })
    navigateAfterAuth(redirectUrl.value)
  } catch (error) {
    showError(error, '登录失败')
  } finally {
    submitting.value = false
  }
}

async function submitWechatLogin() {
  wechatLoading.value = true
  try {
    const code = await getWechatLoginCode()
    await memberStore.wechatLogin({
      code,
      phone: isValidPhone(form.phone) ? normalizePhone(form.phone) : undefined,
    })
    navigateAfterAuth(redirectUrl.value)
  } catch (error) {
    showError(error, '微信登录失败')
  } finally {
    wechatLoading.value = false
  }
}

function goToRegister() {
  openAppPage(`/pages/auth/register?redirect=${encodeURIComponent(redirectUrl.value)}`)
}
</script>

<style scoped lang="scss">
.form-card {
  margin-top: 24rpx;
}
</style>
