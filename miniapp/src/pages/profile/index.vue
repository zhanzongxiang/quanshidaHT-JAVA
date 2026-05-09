<template>
  <view class="page">
    <view v-if="!profile" class="card">
      <text class="empty-text">资料加载中...</text>
    </view>

    <template v-else>
      <view class="card section">
        <text class="section-title">会员资料</text>
        <text class="section-subtitle">手机号：{{ profile.phone }}</text>
        <text class="section-subtitle">状态：{{ profile.status }}</text>
        <text class="section-subtitle">
          微信绑定：{{ profile.wechatOpenid ? `已绑定 ${profile.wechatOpenid}` : '未绑定' }}
        </text>
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
        <text class="section-title">调试用微信绑定</text>
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
          <button plain @click="reloginWithWechat">重新走微信登录</button>
          <button plain @click="logout">退出登录</button>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useMemberStore } from '@/stores/member'
import { ensureMemberSession } from '@/utils/guards'

const memberStore = useMemberStore()
const saving = ref(false)
const binding = ref(false)

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

onShow(async () => {
  const ready = await ensureMemberSession()
  if (!ready) {
    return
  }
  const latest = await memberStore.fetchProfile()
  profile.value = latest
  syncForm()
})

function syncForm() {
  if (!memberStore.profile) {
    return
  }
  form.nickname = memberStore.profile.nickname || ''
  form.fullName = memberStore.profile.fullName || ''
  form.avatarUrl = memberStore.profile.avatarUrl || ''
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
    uni.showToast({
      title: '微信已绑定',
      icon: 'success',
    })
  } finally {
    binding.value = false
  }
}

async function reloginWithWechat() {
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
