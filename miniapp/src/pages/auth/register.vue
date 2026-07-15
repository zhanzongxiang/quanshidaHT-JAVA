<template>
  <view class="page">
    <view class="card">
      <text class="section-title">Create Member Account</text>
      <text class="section-subtitle">
        Registration signs you in immediately, then you can view your profile and accessible waybills.
      </text>
    </view>

    <view class="card form-card">
      <view class="field-stack">
        <view class="field-block">
          <text class="field-label">Phone</text>
          <input
            v-model="form.phone"
            class="input"
            type="number"
            maxlength="11"
            placeholder="Enter 11-digit phone"
            @input="handlePhoneInput"
          />
        </view>
        <view class="field-block">
          <text class="field-label">Password</text>
          <input
            v-model.trim="form.password"
            class="input"
            password
            placeholder="Enter at least 6 characters"
          />
        </view>
        <view class="field-block">
          <text class="field-label">Nickname</text>
          <input v-model.trim="form.nickname" class="input" maxlength="64" placeholder="Optional nickname" />
        </view>
        <view class="field-block">
          <text class="field-label">Full name</text>
          <input v-model.trim="form.fullName" class="input" maxlength="64" placeholder="Optional full name" />
        </view>
      </view>

      <view class="actions top-gap">
        <button class="button-primary" :loading="submitting" @click="submitRegister">Create Account</button>
        <button plain @click="goToLogin">Back to Sign In</button>
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
    showError('Please enter a valid 11-digit phone number')
    return false
  }

  if (!isValidPassword(form.password)) {
    showError('Password must be at least 6 characters')
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
    showError(error, 'Registration failed')
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
