<template>
  <view class="page">
    <view class="card section">
      <view class="row-between header-row">
        <view class="header-copy">
          <text class="section-title">联调工作台</text>
          <text class="section-subtitle">
            在微信开发者工具里按顺序完成环境确认、链路走查和观察记录，并把备注直接留在本地。
          </text>
        </view>
        <text class="pill">{{ completedCount }}/{{ totalCount }}</text>
      </view>
      <view class="progress-track">
        <view class="progress-bar" :style="{ width: progressWidth }"></view>
      </view>
      <view class="actions top-gap">
        <button class="button-primary" @click="copySummary">复制摘要</button>
        <button class="button-secondary" @click="resetChecklist">重置清单</button>
        <button plain @click="openDiagnostics">打开环境诊断</button>
      </view>
    </view>

    <view class="card section">
      <text class="section-title">环境确认</text>
      <view class="checklist">
        <view v-for="item in envItems" :key="item.key" class="check-card">
          <view class="check-row" @click="toggleItem(item.key)">
            <view class="check-mark" :class="{ done: checklistState[item.key]?.checked }">
              <text>{{ checklistState[item.key]?.checked ? '✓' : '' }}</text>
            </view>
            <text class="check-item">{{ item.text }}</text>
          </view>
          <textarea
            v-model="checklistState[item.key].note"
            class="textarea note-input"
            auto-height
            maxlength="120"
            placeholder="可选备注：记录当前环境、账号或异常现象"
            @blur="persistChecklistState"
          />
        </view>
      </view>
    </view>

    <view class="card section">
      <text class="section-title">推荐联调顺序</text>
      <view class="checklist">
        <view v-for="item in flowItems" :key="item.key" class="check-card">
          <view class="check-row" @click="toggleItem(item.key)">
            <view class="check-mark" :class="{ done: checklistState[item.key]?.checked }">
              <text>{{ checklistState[item.key]?.checked ? '✓' : '' }}</text>
            </view>
            <text class="check-item">{{ item.text }}</text>
          </view>
          <textarea
            v-model="checklistState[item.key].note"
            class="textarea note-input"
            auto-height
            maxlength="120"
            placeholder="可选备注：记录页面表现、接口参数或跳转结果"
            @blur="persistChecklistState"
          />
        </view>
      </view>
    </view>

    <view class="card section">
      <text class="section-title">快捷入口</text>
      <view class="actions top-gap">
        <button class="button-primary" @click="openLogin">打开登录页</button>
        <button class="button-secondary" @click="openWaybills">打开运单列表</button>
        <button class="button-secondary" @click="openPayments">打开支付记录</button>
        <button class="button-secondary" @click="openProfile">打开资料页</button>
        <button class="button-secondary" @click="openDiagnostics">打开环境诊断</button>
      </view>
    </view>

    <view class="card section">
      <text class="section-title">重点观察</text>
      <view class="checklist">
        <view v-for="item in observeItems" :key="item.key" class="check-card">
          <view class="check-row" @click="toggleItem(item.key)">
            <view class="check-mark" :class="{ done: checklistState[item.key]?.checked }">
              <text>{{ checklistState[item.key]?.checked ? '✓' : '' }}</text>
            </view>
            <text class="check-item">{{ item.text }}</text>
          </view>
          <textarea
            v-model="checklistState[item.key].note"
            class="textarea note-input"
            auto-height
            maxlength="120"
            placeholder="可选备注：记录异常文案、状态差异或复现步骤"
            @blur="persistChecklistState"
          />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import {
  clearChecklistState,
  getChecklistState,
  setChecklistState,
  type ChecklistEntryState,
  type ChecklistState,
} from '@/utils/debug'
import { openAppPage } from '@/utils/navigation'
import { showSuccess } from '@/utils/toast'

interface ChecklistItem {
  key: string
  text: string
}

const envItems: ChecklistItem[] = [
  { key: 'env_backend', text: '后端已启动，且 VITE_API_BASE_URL 指向当前可访问地址' },
  { key: 'env_merchant', text: '当前商户状态为 Ready，或已明确仍处于 mock 模式' },
  { key: 'env_appid', text: '小程序 appid、后端支付 appId 和域名配置与当前环境一致' },
]

const flowItems: ChecklistItem[] = [
  { key: 'flow_login', text: '先进入登录页，验证手机号登录或微信登录链路' },
  { key: 'flow_home', text: '回到首页，确认会员资料和微信绑定状态已刷新' },
  { key: 'flow_waybill', text: '进入运单列表，并点一条运单进入详情页' },
  { key: 'flow_payment_prepare', text: '从运单详情发起支付准备，观察支付结果页轮询反馈' },
  { key: 'flow_payment_result', text: '回到支付记录，验证可再次查看单笔支付结果' },
  { key: 'flow_profile', text: '进入资料页，验证微信状态刷新、重走微信登录和手动绑定表单' },
]

const observeItems: ChecklistItem[] = [
  { key: 'observe_redirect', text: '未登录进入受限页时，是否先跳登录并在成功后回到原页面' },
  { key: 'observe_payment_message', text: '支付失败、取消或处理中时，结果页提示是否清晰' },
  { key: 'observe_status_copy', text: '首页、支付记录页和资料页的状态文案是否统一' },
  { key: 'observe_wechat_sync', text: '资料页刷新微信状态后，openid 和 unionid 是否同步正确' },
]

const checklistState = reactive<ChecklistState>({})
const allItems = [...envItems, ...flowItems, ...observeItems]

const totalCount = allItems.length
const completedCount = computed(() => allItems.filter((item) => checklistState[item.key]?.checked).length)
const progressWidth = computed(() => `${Math.round((completedCount.value / totalCount) * 100)}%`)

loadChecklistState()

function createEmptyEntry(): ChecklistEntryState {
  return {
    checked: false,
    note: '',
  }
}

function ensureEntry(key: string) {
  if (!checklistState[key]) {
    checklistState[key] = createEmptyEntry()
  }
}

function loadChecklistState() {
  const saved = getChecklistState()
  allItems.forEach((item) => {
    checklistState[item.key] = saved[item.key] || createEmptyEntry()
  })
}

function persistChecklistState() {
  const nextState: ChecklistState = {}
  allItems.forEach((item) => {
    ensureEntry(item.key)
    nextState[item.key] = {
      checked: Boolean(checklistState[item.key].checked),
      note: checklistState[item.key].note.trim(),
    }
    checklistState[item.key].note = nextState[item.key].note
  })
  setChecklistState(nextState)
}

function toggleItem(key: string) {
  ensureEntry(key)
  checklistState[key].checked = !checklistState[key].checked
  persistChecklistState()
}

function resetChecklist() {
  clearChecklistState()
  allItems.forEach((item) => {
    checklistState[item.key] = createEmptyEntry()
  })
  showSuccess('清单已重置')
}

function buildSummary() {
  const sections = [
    { title: '环境确认', items: envItems },
    { title: '联调顺序', items: flowItems },
    { title: '重点观察', items: observeItems },
  ]

  const lines = ['联调工作台摘要', `完成进度：${completedCount.value}/${totalCount}`]

  sections.forEach((section) => {
    lines.push('', `[${section.title}]`)
    section.items.forEach((item) => {
      const entry = checklistState[item.key] || createEmptyEntry()
      const status = entry.checked ? '已完成' : '未完成'
      const note = entry.note ? `；备注：${entry.note}` : ''
      lines.push(`- ${status} ${item.text}${note}`)
    })
  })

  return lines.join('\n')
}

function copySummary() {
  persistChecklistState()
  uni.setClipboardData({
    data: buildSummary(),
    success: () => {
      showSuccess('摘要已复制')
    },
  })
}

function openLogin() {
  openAppPage('/pages/auth/login')
}

function openWaybills() {
  openAppPage('/pages/waybill/list')
}

function openPayments() {
  openAppPage('/pages/payment/list')
}

function openProfile() {
  openAppPage('/pages/profile/index')
}

function openDiagnostics() {
  openAppPage('/pages/debug/diagnostics')
}
</script>

<style scoped lang="scss">
.section {
  margin-bottom: 24rpx;
}

.header-row {
  align-items: flex-start;
}

.header-copy {
  flex: 1;
}

.progress-track {
  height: 12rpx;
  margin-top: 20rpx;
  border-radius: 999rpx;
  background: #ece2d2;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #0e6b56 0%, #6e9f8b 100%);
}

.checklist {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 20rpx;
}

.check-card {
  padding: 20rpx;
  border-radius: 20rpx;
  background: #fffaf1;
  border: 2rpx solid #eadfcd;
}

.check-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}

.check-mark {
  width: 36rpx;
  height: 36rpx;
  flex: none;
  border: 2rpx solid #c7b89e;
  border-radius: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  background: #fffdf8;
}

.check-mark.done {
  border-color: #0e6b56;
  background: #0e6b56;
}

.check-item {
  flex: 1;
  line-height: 1.6;
  color: #4f544c;
}

.note-input {
  margin-top: 16rpx;
}
</style>
