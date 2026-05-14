<template>
  <view class="page">
    <view class="card">
      <text class="section-title">会员注册</text>
      <text class="section-subtitle">注册完成后会自动登录，并可直接查看会员资料与可见运单。</text>
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
        <view class="field-block">
          <text class="field-label">昵称</text>
          <input v-model.trim="form.nickname" class="input" maxlength="20" placeholder="可选，最多 20 个字符" />
        </view>
        <view class="field-block">
          <text class="field-label">姓名</text>
          <input v-model.trim="form.fullName" class="input" maxlength="20" placeholder="可选，最多 20 个字符" />
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
import { navigateAfterAuth, openAppPage, resolveRedirectUrl } from '@/utils/navigation'
import { showError } from '@/utils/toast'
import { isValidPassword, isValidPhone, normalizePhone } from '@/utils/validation'

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
  redirectUrl.value = resolveRedirectUrl(typeof query?.redirect === 'string' ? query.redirect : undefined)
})

function handlePhoneInput(event: Event) {
  const value = (event as { detail?: { value?: string } }).detail?.value || ''
  form.phone = normalizePhone(value)
}

function validateRegisterForm() {
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

async function submitRegister() {
  if (!validateRegisterForm()) {
    return
  }

  submitting.value = true
  try {
    await memberStore.register({
      phone: normalizePhone(form.phone),
      password: form.password.trim(),
      nickname: form.nickname.trim(),
      fullName: form.fullName.trim(),
    })
    navigateAfterAuth(redirectUrl.value)
  } catch (error) {
    showError(error, '注册失败')
  } finally {
    submitting.value = false
  }
}

function goToLogin() {
  openAppPage(`/pages/auth/login?redirect=${encodeURIComponent(redirectUrl.value)}`)
}
</script>

<style scoped lang="scss">
.form-card {
  margin-top: 24rpx;
}
</style>
