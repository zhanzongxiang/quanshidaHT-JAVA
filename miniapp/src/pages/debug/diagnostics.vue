<template>
  <view class="page">
    <view class="card section">
      <text class="section-title">环境诊断</text>
      <text class="section-subtitle">
        用于在微信开发者工具里快速确认接口地址、登录态、微信绑定状态和最近一次接口结果。
      </text>
      <view class="actions top-gap">
        <button class="button-primary" @click="copySummary">复制诊断摘要</button>
        <button class="button-secondary" @click="refreshPanel">刷新面板</button>
      </view>
    </view>

    <view class="card section">
      <text class="section-title">当前环境</text>
      <text class="detail-line">后端地址：{{ apiBaseUrl }}</text>
      <text class="detail-line">登录态：{{ memberStore.isAuthenticated ? '已登录' : '未登录' }}</text>
      <text class="detail-line">Token：{{ maskedToken }}</text>
      <text class="detail-line">微信绑定：{{ wechatStatus }}</text>
    </view>

    <view class="card section">
      <text class="section-title">最近一次接口结果</text>
      <view v-if="lastApiEvent">
        <text class="detail-line">结果：{{ lastApiEvent.type === 'success' ? '成功' : '失败' }}</text>
        <text class="detail-line">接口：{{ lastApiEvent.method }} {{ lastApiEvent.url }}</text>
        <text class="detail-line">状态码：{{ lastApiEvent.statusCode || '无' }}</text>
        <text class="detail-line">信息：{{ lastApiEvent.message }}</text>
        <text class="detail-line">时间：{{ lastApiEvent.at }}</text>
      </view>
      <text v-else class="empty-text">当前还没有接口记录</text>
    </view>

    <view class="card section">
      <text class="section-title">快捷诊断动作</text>
      <view class="actions top-gap">
        <button class="button-primary" :loading="testingProfile" @click="testProfile">测试资料接口</button>
        <button class="button-secondary" @click="clearApiRecord">清空最近接口记录</button>
        <button plain @click="clearSession">清空本地登录态</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { API_BASE_URL } from '@/config/env'
import { useMemberStore } from '@/stores/member'
import { clearLastApiEvent, getLastApiEvent, type LastApiEvent } from '@/utils/debug'
import { formatWechatBindStatus, maskIdentifier } from '@/utils/display'

const memberStore = useMemberStore()
const apiBaseUrl = API_BASE_URL
const testingProfile = ref(false)
const lastApiEvent = ref<LastApiEvent | null>(null)

const maskedToken = computed(() => maskIdentifier(memberStore.token))
const wechatStatus = computed(() => formatWechatBindStatus(memberStore.profile?.wechatOpenid))

onShow(async () => {
  await refreshPanel()
})

async function refreshPanel() {
  memberStore.restore()
  lastApiEvent.value = getLastApiEvent()

  if (memberStore.isAuthenticated) {
    try {
      await memberStore.fetchProfile()
    } catch {
      memberStore.logout(false)
    }
    lastApiEvent.value = getLastApiEvent()
  }
}

function buildSummary() {
  const lines = [
    '环境诊断摘要',
    `后端地址：${apiBaseUrl}`,
    `登录态：${memberStore.isAuthenticated ? '已登录' : '未登录'}`,
    `Token：${maskedToken.value}`,
    `微信绑定：${wechatStatus.value}`,
  ]

  if (lastApiEvent.value) {
    lines.push(
      `最近接口结果：${lastApiEvent.value.type === 'success' ? '成功' : '失败'}`,
      `最近接口地址：${lastApiEvent.value.method} ${lastApiEvent.value.url}`,
      `最近接口状态码：${lastApiEvent.value.statusCode || '无'}`,
      `最近接口信息：${lastApiEvent.value.message}`,
      `最近接口时间：${lastApiEvent.value.at}`,
    )
  } else {
    lines.push('最近接口结果：暂无记录')
  }

  return lines.join('\n')
}

function copySummary() {
  uni.setClipboardData({
    data: buildSummary(),
    success: () => {
      uni.showToast({
        title: '诊断摘要已复制',
        icon: 'success',
      })
    },
  })
}

async function testProfile() {
  if (!memberStore.isAuthenticated) {
    uni.showToast({
      title: '当前未登录',
      icon: 'none',
    })
    return
  }

  testingProfile.value = true
  try {
    await memberStore.fetchProfile()
    uni.showToast({
      title: '资料接口正常',
      icon: 'success',
    })
  } finally {
    testingProfile.value = false
    lastApiEvent.value = getLastApiEvent()
  }
}

function clearApiRecord() {
  clearLastApiEvent()
  lastApiEvent.value = null
  uni.showToast({
    title: '已清空',
    icon: 'success',
  })
}

function clearSession() {
  memberStore.logout(false)
  uni.showToast({
    title: '本地登录态已清空',
    icon: 'success',
  })
  void refreshPanel()
}
</script>

<style scoped lang="scss">
.section {
  margin-bottom: 24rpx;
}

.detail-line {
  display: block;
  margin-top: 10rpx;
  line-height: 1.6;
  color: #4f544c;
  word-break: break-all;
}

.top-gap {
  margin-top: 20rpx;
}
</style>
