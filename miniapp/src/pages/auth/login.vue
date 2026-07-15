<template>
  <view class="page">
    <view class="card">
      <text class="section-title">Member Sign In</text>
      <text class="section-subtitle">
        Use phone/password, or continue with WeChat. If the WeChat account is new, finish with a phone number.
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
            placeholder="Enter password"
          />
        </view>
      </view>

      <view class="actions top-gap">
        <button class="button-primary" :loading="submitting" @click="submitLogin">Sign In</button>
        <button class="button-secondary" :loading="wechatLoading" @click="submitWechatLogin">
          WeChat Sign In
        </button>
        <button plain @click="goToRegister">Create Account</button>
      </view>
    </view>

    <view v-if="pendingBindTicket" class="card form-card">
      <text class="section-title">Complete WeChat Sign In</text>
      <text class="section-subtitle">
        This WeChat account is not linked yet. Confirm a phone number to create or link a member account.
      </text>
      <view class="actions top-gap">
        <button class="button-primary" :loading="completionLoading" @click="completeWechatLogin(false)">
          Complete Sign In
        </button>
        <button class="button-secondary" :loading="completionLoading" @click="completeWechatLogin(true)">
          Complete And Replace Binding
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useMemberStore } from '@/stores/member'
import { navigateAfterAuth, openAppPage, resolveRedirectUrl } from '@/utils/navigation'
import { showError, showSuccess } from '@/utils/toast'
import { isValidPassword, isValidPhone, normalizePhone } from '@/utils/validation'
import { getWechatLoginCode } from '@/utils/wechat'

const memberStore = useMemberStore()
const submitting = ref(false)
const wechatLoading = ref(false)
const completionLoading = ref(false)
const redirectUrl = ref('/pages/index/index')
const pendingBindTicket = ref('')

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
    showError('Please enter a valid 11-digit phone number')
    return false
  }

  if (!isValidPassword(form.password)) {
    showError('Password must be at least 6 characters')
    return false
  }

  return true
}

function validatePhoneForWechat() {
  if (!isValidPhone(form.phone)) {
    showError('Please enter a valid 11-digit phone number')
    return false
  }
  return true
}

async function withLoading(flag: typeof submitting, task: () => Promise<void>) {
  flag.value = true
  try {
    await task()
  } finally {
    flag.value = false
  }
}

async function submitLogin() {
  if (!validateCredentials()) {
    return
  }

  try {
    await withLoading(submitting, async () => {
      await memberStore.login({
        phone: normalizePhone(form.phone),
        password: form.password.trim(),
      })
      navigateAfterAuth(redirectUrl.value)
    })
  } catch (error) {
    showError(error, 'Sign in failed')
  }
}

async function submitWechatLogin() {
  try {
    await withLoading(wechatLoading, async () => {
      const code = await getWechatLoginCode()
      const result = await memberStore.wechatLogin({
        code,
        phone: isValidPhone(form.phone) ? normalizePhone(form.phone) : undefined,
      })

      if (result.phoneCompletionRequired && result.bindTicket) {
        pendingBindTicket.value = result.bindTicket
        showSuccess('Enter a phone number to finish WeChat sign in')
        return
      }

      pendingBindTicket.value = ''
      navigateAfterAuth(redirectUrl.value)
    })
  } catch (error) {
    showError(error, 'WeChat sign in failed')
  }
}

async function completeWechatLogin(replaceBinding: boolean) {
  if (!pendingBindTicket.value) {
    showError('WeChat sign-in session is missing')
    return
  }
  if (!validatePhoneForWechat()) {
    return
  }

  try {
    await withLoading(completionLoading, async () => {
      const result = await memberStore.completeWechatLogin({
        bindTicket: pendingBindTicket.value,
        phone: normalizePhone(form.phone),
        replaceBinding,
      })

      if (result.phoneCompletionRequired) {
        showError('WeChat sign-in is still waiting for phone confirmation')
        return
      }

      pendingBindTicket.value = ''
      navigateAfterAuth(redirectUrl.value)
    })
  } catch (error) {
    showError(error, 'WeChat sign-in completion failed')
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
