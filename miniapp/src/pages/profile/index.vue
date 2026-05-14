<template>
  <view class="page">
    <view v-if="!profile" class="card">
      <text class="empty-text">资料加载中...</text>
    </view>

    <template v-else>
      <view class="card section">
        <view class="row-between">
          <text class="section-title">会员资料</text>
          <text class="pill">{{ profileStatusText }}</text>
        </view>
        <text class="detail-line">手机号：{{ profile.phone }}</text>
        <text class="detail-line">昵称：{{ profile.nickname || '未填写' }}</text>
        <text class="detail-line">姓名：{{ profile.fullName || '未填写' }}</text>
        <text class="detail-line muted">注册时间：{{ profile.createdAt }}</text>
      </view>

      <view class="card section">
        <text class="section-title">微信绑定状态</text>
        <text class="section-subtitle">{{ wechatStatusLabel }}</text>
        <text class="detail-line">{{ wechatSummary }}</text>
        <text v-if="profile.wechatBindTime" class="detail-line">绑定时间：{{ profile.wechatBindTime }}</text>
        <text v-if="profile.wechatOpenid" class="detail-line">OpenID：{{ maskedWechatOpenid }}</text>
        <text v-if="profile.wechatUnionid" class="detail-line">UnionID：{{ maskedWechatUnionid }}</text>
        <view class="actions top-gap">
          <button class="button-secondary" :loading="wechatLoggingIn" @click="reloginWithWechat">重新走微信登录</button>
          <button plain :loading="refreshing" @click="handleRefreshProfile">刷新资料</button>
        </view>
      </view>

      <view class="card section">
        <text class="section-title">编辑资料</text>
        <text class="section-subtitle">修改昵称、姓名和头像地址，保存后会直接同步当前会员资料。</text>
        <view class="field-stack top-gap">
          <view class="field-block">
            <text class="field-label">昵称</text>
            <input v-model.trim="form.nickname" class="input" maxlength="64" placeholder="请输入昵称" />
          </view>
          <view class="field-block">
            <text class="field-label">姓名</text>
            <input v-model.trim="form.fullName" class="input" maxlength="64" placeholder="请输入姓名" />
          </view>
          <view class="field-block">
            <text class="field-label">头像地址</text>
            <input v-model.trim="form.avatarUrl" class="input" maxlength="500" placeholder="请输入头像 URL，可留空" />
          </view>
        </view>
        <view class="actions top-gap">
          <button class="button-primary" :loading="saving" @click="saveProfile">保存资料</button>
        </view>
      </view>

      <view class="card section">
        <text class="section-title">手工绑定微信</text>
        <text class="section-subtitle">
          当前后端保留了按 openid / unionid 直接绑定的接口，这里提供联调入口，方便排查登录和资料同步问题。
        </text>
        <view class="field-stack top-gap">
          <view class="field-block">
            <text class="field-label">OpenID</text>
            <input v-model.trim="wechatForm.openid" class="input" maxlength="64" placeholder="请输入 openid" />
          </view>
          <view class="field-block">
            <text class="field-label">UnionID</text>
            <input v-model.trim="wechatForm.unionid" class="input" maxlength="64" placeholder="可选" />
          </view>
        </view>
        <view class="actions top-gap">
          <button class="button-secondary" :loading="binding" @click="bindWechat">提交绑定</button>
          <button plain @click="logout">退出登录</button>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useMemberStore } from '@/stores/member'
import { formatMemberStatus, formatWechatBindStatus, maskIdentifier } from '@/utils/display'
import { ensureMemberSession } from '@/utils/guards'
import { showError, showSuccess } from '@/utils/toast'
import { getWechatLoginCode } from '@/utils/wechat'

const memberStore = useMemberStore()
const saving = ref(false)
const binding = ref(false)
const refreshing = ref(false)
const wechatLoggingIn = ref(false)

const form = reactive({
  nickname: '',
  fullName: '',
  avatarUrl: '',
})

const wechatForm = reactive({
  openid: '',
  unionid: '',
})

const profile = ref(memberStore.profile)

const profileStatusText = computed(() => formatMemberStatus(profile.value?.status))
const wechatStatusLabel = computed(() => formatWechatBindStatus(profile.value?.wechatOpenid))
const maskedWechatOpenid = computed(() => maskIdentifier(profile.value?.wechatOpenid))
const maskedWechatUnionid = computed(() => maskIdentifier(profile.value?.wechatUnionid))

const wechatSummary = computed(() => {
  if (!profile.value?.wechatOpenid) {
    return '当前账号还未绑定微信身份，可先走微信登录，也可以手工填写 openid 和 unionid 完成联调。'
  }
  return '当前账号已绑定微信身份，可继续验证登录回写、资料刷新和支付前置校验。'
})

onShow(async () => {
  const ready = await ensureMemberSession()
  if (!ready) {
    return
  }
  await refreshProfile(false)
})

function syncForm() {
  if (!memberStore.profile) {
    return
  }

  form.nickname = memberStore.profile.nickname || ''
  form.fullName = memberStore.profile.fullName || ''
  form.avatarUrl = memberStore.profile.avatarUrl || ''
  wechatForm.openid = memberStore.profile.wechatOpenid || ''
  wechatForm.unionid = memberStore.profile.wechatUnionid || ''
}

async function refreshProfile(showToast = true) {
  refreshing.value = true
  try {
    const latest = await memberStore.fetchProfile()
    profile.value = latest
    syncForm()
    if (showToast) {
      showSuccess('资料已刷新')
    }
  } catch (error) {
    showError(error, '资料加载失败')
  } finally {
    refreshing.value = false
  }
}

async function handleRefreshProfile() {
  await refreshProfile()
}

async function saveProfile() {
  saving.value = true
  try {
    const latest = await memberStore.saveProfile({
      nickname: form.nickname.trim(),
      fullName: form.fullName.trim(),
      avatarUrl: form.avatarUrl.trim(),
    })
    profile.value = latest
    syncForm()
    showSuccess('资料已保存')
  } catch (error) {
    showError(error, '保存资料失败')
  } finally {
    saving.value = false
  }
}

async function bindWechat() {
  if (!wechatForm.openid.trim()) {
    showError('请输入 openid')
    return
  }

  binding.value = true
  try {
    const latest = await memberStore.bindWechat({
      openid: wechatForm.openid.trim(),
      unionid: wechatForm.unionid.trim(),
    })
    profile.value = latest
    syncForm()
    showSuccess('微信绑定成功')
  } catch (error) {
    showError(error, '微信绑定失败')
  } finally {
    binding.value = false
  }
}

async function reloginWithWechat() {
  wechatLoggingIn.value = true
  try {
    const code = await getWechatLoginCode()
    await memberStore.wechatLogin({
      code,
      phone: profile.value?.phone || undefined,
    })
    profile.value = memberStore.profile
    syncForm()
    showSuccess('微信登录成功')
  } catch (error) {
    showError(error, '微信登录失败')
  } finally {
    wechatLoggingIn.value = false
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
