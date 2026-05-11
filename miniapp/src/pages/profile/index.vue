<template>
  <view class="page">
    <view v-if="!profile" class="card">
      <text class="empty-text">资料加载中...</text>
    </view>

    <template v-else>
      <view class="card section">
        <view class="row-between">
          <text class="section-title">会员资料</text>
          <text class="pill">{{ profile.status }}</text>
        </view>
        <text class="section-subtitle">手机号：{{ profile.phone }}</text>
        <text class="section-subtitle">昵称：{{ profile.nickname || '未填写' }}</text>
        <text class="section-subtitle">姓名：{{ profile.fullName || '未填写' }}</text>
      </view>

      <view class="card section">
        <text class="section-title">微信状态</text>
        <text class="section-subtitle">绑定状态：{{ wechatStatusLabel }}</text>
        <text class="section-subtitle">{{ wechatSummary }}</text>
        <text v-if="profile.wechatBindTime" class="section-subtitle">绑定时间：{{ profile.wechatBindTime }}</text>
        <text v-if="profile.wechatOpenid" class="section-subtitle">openid：{{ maskedWechatOpenid }}</text>
        <text v-if="profile.wechatUnionid" class="section-subtitle">unionid：{{ maskedWechatUnionid }}</text>
        <view class="actions top-gap">
          <button class="button-secondary" :loading="wechatLoggingIn" @click="reloginWithWechat">重新走微信登录</button>
          <button plain :loading="refreshing" @click="handleRefreshProfile">刷新资料</button>
        </view>
      </view>

      <view class="card section">
        <text class="section-title">编辑资料</text>
        <view class="field-stack top-gap">
          <view>
            <text class="field-label">昵称</text>
            <input v-model.trim="form.nickname" class="input" placeholder="请输入昵称" />
          </view>
          <view>
            <text class="field-label">姓名</text>
            <input v-model.trim="form.fullName" class="input" placeholder="请输入姓名" />
          </view>
          <view>
            <text class="field-label">头像地址</text>
            <input v-model.trim="form.avatarUrl" class="input" placeholder="请输入头像 URL" />
          </view>
        </view>
        <view class="actions top-gap">
          <button class="button-primary" :loading="saving" @click="saveProfile">保存资料</button>
        </view>
      </view>

      <view class="card section">
        <text class="section-title">手动绑定微信</text>
        <text class="section-subtitle">当前后端提供的是 openid/unionid 绑定接口，这里保留联调用表单。</text>
        <view class="field-stack top-gap">
          <view>
            <text class="field-label">openid</text>
            <input v-model.trim="wechatForm.openid" class="input" placeholder="请输入 openid" />
          </view>
          <view>
            <text class="field-label">unionid</text>
            <input v-model.trim="wechatForm.unionid" class="input" placeholder="可选" />
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
import { formatWechatBindStatus, maskIdentifier } from '@/utils/display'
import { ensureMemberSession } from '@/utils/guards'

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

const wechatSummary = computed(() => {
  if (!profile.value?.wechatOpenid) {
    return '当前未绑定微信，可先走微信登录，也可手动填 openid/unionid 做联调。'
  }
  return '当前账号已绑定微信身份，可继续验证登录回写和资料刷新。'
})

const wechatStatusLabel = computed(() => formatWechatBindStatus(profile.value?.wechatOpenid))
const maskedWechatOpenid = computed(() => maskIdentifier(profile.value?.wechatOpenid))
const maskedWechatUnionid = computed(() => maskIdentifier(profile.value?.wechatUnionid))

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
      uni.showToast({
        title: '资料已刷新',
        icon: 'success',
      })
    }
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
      nickname: form.nickname,
      fullName: form.fullName,
      avatarUrl: form.avatarUrl,
    })
    profile.value = latest
    syncForm()
    uni.showToast({
      title: '资料已保存',
      icon: 'success',
    })
  } finally {
    saving.value = false
  }
}

async function bindWechat() {
  if (!wechatForm.openid) {
    uni.showToast({
      title: 'openid 必填',
      icon: 'none',
    })
    return
  }
  binding.value = true
  try {
    const latest = await memberStore.bindWechat({
      openid: wechatForm.openid,
      unionid: wechatForm.unionid,
    })
    profile.value = latest
    syncForm()
    uni.showToast({
      title: '微信已绑定',
      icon: 'success',
    })
  } finally {
    binding.value = false
  }
}

async function reloginWithWechat() {
  wechatLoggingIn.value = true
  try {
    const result = await uni.login()
    if (!result.code) {
      throw new Error('微信 code 获取失败')
    }
    await memberStore.wechatLogin({
      code: result.code,
      phone: profile.value?.phone || undefined,
    })
    profile.value = memberStore.profile
    syncForm()
    uni.showToast({
      title: '微信登录成功',
      icon: 'success',
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '微信登录失败'
    uni.showToast({
      title: message,
      icon: 'none',
    })
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

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.top-gap {
  margin-top: 24rpx;
}

.input {
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 18rpx;
  border: 2rpx solid #ddd2c2;
  background: #fff;
}
</style>
