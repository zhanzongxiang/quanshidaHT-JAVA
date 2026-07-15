<template>
  <view class="page">
    <view v-if="!profile" class="card">
      <text class="empty-text">Loading profile...</text>
    </view>

    <template v-else>
      <view class="card section">
        <view class="row-between">
          <text class="section-title">Member Profile</text>
          <text class="pill">{{ profileStatusText }}</text>
        </view>
        <text class="detail-line">Phone: {{ profile.phone }}</text>
        <text class="detail-line">Nickname: {{ profile.nickname || 'Not set' }}</text>
        <text class="detail-line">Full name: {{ profile.fullName || 'Not set' }}</text>
        <text class="detail-line">Register source: {{ registerSourceText }}</text>
        <text class="detail-line">Last login: {{ profile.lastLoginAt || 'N/A' }}</text>
        <text class="detail-line">Last login IP: {{ profile.lastLoginIp || 'N/A' }}</text>
        <text class="detail-line">Password updated: {{ profile.passwordUpdatedAt || 'N/A' }}</text>
        <text class="detail-line muted">Created at: {{ profile.createdAt }}</text>
      </view>

      <view class="card section">
        <text class="section-title">WeChat Binding</text>
        <text class="section-subtitle">{{ wechatSummary }}</text>
        <text v-if="profile.wechatBindTime" class="detail-line">Bound at: {{ profile.wechatBindTime }}</text>
        <text v-if="profile.wechatOpenid" class="detail-line">OpenID: {{ maskedWechatOpenid }}</text>
        <text v-if="profile.wechatUnionid" class="detail-line">UnionID: {{ maskedWechatUnionid }}</text>
        <view class="actions top-gap">
          <button class="button-secondary" :loading="binding" @click="bindCurrentWechat">
            {{ profile.wechatOpenid ? 'Rebind Current WeChat' : 'Bind Current WeChat' }}
          </button>
          <button v-if="profile.wechatOpenid" plain :loading="unbinding" @click="unbindWechat">
            Unbind WeChat
          </button>
          <button plain :loading="refreshing" @click="handleRefreshProfile">Refresh</button>
        </view>
      </view>

      <view class="card section">
        <text class="section-title">Edit Profile</text>
        <text class="section-subtitle">Update nickname, full name, and avatar URL for the current member account.</text>
        <view class="field-stack top-gap">
          <view class="field-block">
            <text class="field-label">Nickname</text>
            <input v-model.trim="form.nickname" class="input" maxlength="64" placeholder="Enter nickname" />
          </view>
          <view class="field-block">
            <text class="field-label">Full name</text>
            <input v-model.trim="form.fullName" class="input" maxlength="64" placeholder="Enter full name" />
          </view>
          <view class="field-block">
            <text class="field-label">Avatar URL</text>
            <input v-model.trim="form.avatarUrl" class="input" maxlength="500" placeholder="Enter avatar URL" />
          </view>
        </view>
        <view class="actions top-gap">
          <button class="button-primary" :loading="saving" @click="saveProfile">Save Profile</button>
        </view>
      </view>

      <view class="card section">
        <text class="section-title">Change Password</text>
        <text class="section-subtitle">Use a new password with at least 6 characters.</text>
        <view class="field-stack top-gap">
          <view class="field-block">
            <text class="field-label">Current password</text>
            <input
              v-model.trim="passwordForm.currentPassword"
              class="input"
              password
              maxlength="64"
              placeholder="Enter current password"
            />
          </view>
          <view class="field-block">
            <text class="field-label">New password</text>
            <input
              v-model.trim="passwordForm.newPassword"
              class="input"
              password
              maxlength="64"
              placeholder="Enter new password"
            />
          </view>
        </view>
        <view class="actions top-gap">
          <button class="button-primary" :loading="changingPassword" @click="changePassword">Change Password</button>
          <button plain @click="logout">Sign Out</button>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useMemberStore } from '@/stores/member'
import { ensureMemberSession } from '@/utils/guards'
import { showError, showSuccess } from '@/utils/toast'
import { isValidPassword } from '@/utils/validation'
import { getWechatLoginCode } from '@/utils/wechat'

const memberStore = useMemberStore()
const saving = ref(false)
const binding = ref(false)
const unbinding = ref(false)
const refreshing = ref(false)
const changingPassword = ref(false)

const form = reactive({
  nickname: '',
  fullName: '',
  avatarUrl: '',
})

const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
})

const profile = ref(memberStore.profile)

const profileStatusText = computed(() => {
  if (profile.value?.status === 'active') return 'Active'
  if (profile.value?.status === 'disabled') return 'Disabled'
  if (profile.value?.status === 'pending') return 'Pending Review'
  return profile.value?.status || 'Unknown'
})

const registerSourceText = computed(() => {
  if (profile.value?.registerSource === 'miniapp_phone') return 'Miniapp phone registration'
  if (profile.value?.registerSource === 'miniapp_wechat') return 'Miniapp WeChat registration'
  if (profile.value?.registerSource === 'admin_created') return 'Admin created'
  return profile.value?.registerSource || 'Unknown'
})

const maskedWechatOpenid = computed(() => maskIdentifier(profile.value?.wechatOpenid))
const maskedWechatUnionid = computed(() => maskIdentifier(profile.value?.wechatUnionid))
const wechatSummary = computed(() => {
  if (!profile.value?.wechatOpenid) {
    return 'No WeChat account is bound yet.'
  }
  return 'This member account is linked to a WeChat identity.'
})

onShow(async () => {
  const ready = await ensureMemberSession()
  if (!ready) {
    return
  }

  await refreshProfile(false)
})

function maskIdentifier(value?: string | null) {
  if (!value) {
    return 'N/A'
  }
  if (value.length <= 8) {
    return value
  }
  return `${value.slice(0, 4)}...${value.slice(-4)}`
}

function syncForm() {
  if (!memberStore.profile) {
    return
  }

  form.nickname = memberStore.profile.nickname || ''
  form.fullName = memberStore.profile.fullName || ''
  form.avatarUrl = memberStore.profile.avatarUrl || ''
}

function resetPasswordForm() {
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
}

async function withLoading(flag: typeof saving, task: () => Promise<void>) {
  flag.value = true
  try {
    await task()
  } finally {
    flag.value = false
  }
}

async function refreshProfile(showToast = true) {
  try {
    await withLoading(refreshing, async () => {
      const latest = await memberStore.fetchProfile()
      profile.value = latest
      syncForm()
    })

    if (showToast) {
      showSuccess('Profile refreshed')
    }
  } catch (error) {
    showError(error, 'Failed to load profile')
  }
}

async function handleRefreshProfile() {
  await refreshProfile()
}

async function saveProfile() {
  try {
    await withLoading(saving, async () => {
      const latest = await memberStore.saveProfile({
        nickname: form.nickname.trim(),
        fullName: form.fullName.trim(),
        avatarUrl: form.avatarUrl.trim(),
      })
      profile.value = latest
      syncForm()
    })
    showSuccess('Profile saved')
  } catch (error) {
    showError(error, 'Failed to save profile')
  }
}

async function confirmAction(title: string, content: string) {
  const result = await uni.showModal({
    title,
    content,
    confirmText: 'Continue',
    cancelText: 'Cancel',
  })
  return Boolean(result.confirm)
}

async function bindCurrentWechat() {
  const replaceBinding = Boolean(profile.value?.wechatOpenid)
  if (replaceBinding) {
    const confirmed = await confirmAction(
      'Replace WeChat binding',
      'This will replace the current WeChat binding for this member account.',
    )
    if (!confirmed) {
      return
    }
  }

  try {
    await withLoading(binding, async () => {
      const code = await getWechatLoginCode()
      const latest = await memberStore.bindWechat({
        code,
        replaceBinding,
      })
      profile.value = latest
      syncForm()
    })
    showSuccess(replaceBinding ? 'WeChat binding replaced' : 'WeChat bound')
  } catch (error) {
    showError(error, 'Failed to bind WeChat')
  }
}

async function unbindWechat() {
  const confirmed = await confirmAction(
    'Unbind WeChat',
    'This will remove the current WeChat binding from this member account.',
  )
  if (!confirmed) {
    return
  }

  try {
    await withLoading(unbinding, async () => {
      const latest = await memberStore.unbindWechat()
      profile.value = latest
      syncForm()
    })
    showSuccess('WeChat unbound')
  } catch (error) {
    showError(error, 'Failed to unbind WeChat')
  }
}

async function changePassword() {
  if (!isValidPassword(passwordForm.currentPassword)) {
    showError('Current password must be at least 6 characters')
    return
  }
  if (!isValidPassword(passwordForm.newPassword)) {
    showError('New password must be at least 6 characters')
    return
  }

  try {
    await withLoading(changingPassword, async () => {
      await memberStore.updatePassword({
        currentPassword: passwordForm.currentPassword.trim(),
        newPassword: passwordForm.newPassword.trim(),
      })
    })
    resetPasswordForm()
    await refreshProfile(false)
    showSuccess('Password changed')
  } catch (error) {
    showError(error, 'Failed to change password')
  }
}

function logout() {
  memberStore.logout()
}
</script>

<style scoped lang="scss">
.section {
  margin-bottom: 24rpx;
}
</style>
