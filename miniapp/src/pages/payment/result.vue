<template>
  <view class="page">
    <view class="card result-card">
      <text class="pill">{{ resultLabel }}</text>
      <text class="section-title">{{ titleText }}</text>
      <text class="section-subtitle">{{ subtitleText }}</text>
      <view class="result-lines">
        <text class="detail-line">支付单号：{{ orderNo || '暂无' }}</text>
        <text class="detail-line">状态：{{ formatPaymentStatus(status || 'timeout') }}</text>
        <text class="detail-line">轮询次数：{{ pollCount }}</text>
        <text v-if="statusMessage" class="detail-line">提示：{{ statusMessage }}</text>
      </view>
    </view>

    <view v-if="currentPayment" class="card">
      <text class="section-title">订单信息</text>
      <text class="detail-line">商户：{{ currentPayment.merchantName }}</text>
      <text class="detail-line">运单：{{ currentPayment.waybillTrackingNo || '未关联运单' }}</text>
      <text class="detail-line">金额：{{ currentPayment.amountTotal }}</text>
      <text class="detail-line">创建时间：{{ currentPayment.createdAt }}</text>
      <text class="detail-line">支付时间：{{ currentPayment.paidAt || '未支付' }}</text>
    </view>

    <view class="card">
      <text class="section-title">下一步</text>
      <view class="actions top-gap">
        <button class="button-primary" @click="openPaymentList">查看支付记录</button>
        <button class="button-secondary" @click="openWaybills">返回运单列表</button>
        <button plain :disabled="polling" @click="pollNow">{{ polling ? '刷新中...' : '立即刷新状态' }}</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { fetchMemberPayments } from '@/api/payment'
import type { MemberPayOrderSummary } from '@/types/payment'
import { formatPaymentStatus } from '@/utils/display'
import { ensureMemberSession } from '@/utils/guards'
import { openAppPage } from '@/utils/navigation'

const MAX_POLL_COUNT = 6
const FINAL_STATUSES = ['paid', 'closed', 'exception', 'refunded']

const orderNo = ref('')
const status = ref('')
const statusMessage = ref('')
const pollCount = ref(0)
const polling = ref(false)
const currentPayment = ref<MemberPayOrderSummary | null>(null)
let timer: ReturnType<typeof setTimeout> | null = null

const resultLabel = computed(() => {
  if (status.value === 'paid') return '支付成功'
  if (status.value === 'closed') return '支付关闭'
  if (status.value === 'exception') return '支付异常'
  if (status.value === 'refunding') return '退款处理中'
  if (status.value === 'refunded') return '已退款'
  if (status.value === 'timeout') return '状态待确认'
  return '支付处理中'
})

const titleText = computed(() => {
  if (status.value === 'paid') return '订单已支付完成'
  if (status.value === 'closed') return '订单已关闭'
  if (status.value === 'exception') return '订单进入异常状态'
  if (status.value === 'timeout') return '支付状态仍在同步中'
  return '正在同步支付状态'
})

const subtitleText = computed(() => {
  if (status.value === 'paid') return '后端状态已经更新，可以回到支付记录或运单列表继续操作。'
  if (status.value === 'closed') return '这笔支付已关闭，如需继续支付，请重新发起。'
  if (status.value === 'exception') return '建议去支付记录页查看详情，并结合后台订单状态排查。'
  if (status.value === 'timeout') return '小程序端轮询已经停止，请稍后手动刷新，或去支付记录页确认最终结果。'
  return '小程序支付完成后，后端回调和状态同步可能需要一点时间。'
})

onLoad(async (query) => {
  const ready = await ensureMemberSession()
  if (!ready) {
    return
  }

  orderNo.value = String(query?.orderNo || '').trim()
  status.value = String(query?.status || 'paying').trim()
  statusMessage.value = String(query?.message || '').trim()
  await pollStatus()
})

onUnload(() => {
  clearTimer()
})

async function pollStatus() {
  if (polling.value) {
    return
  }

  if (!orderNo.value) {
    if (!statusMessage.value && status.value === 'exception') {
      statusMessage.value = '未获取到支付单号，请回到支付记录页核对后台状态。'
    }
    return
  }

  polling.value = true
  pollCount.value += 1
  try {
    const payments = await fetchMemberPayments()
    currentPayment.value = payments.find((item) => item.orderNo === orderNo.value) || null

    if (currentPayment.value) {
      status.value = currentPayment.value.status
    } else if (!statusMessage.value) {
      statusMessage.value = '当前支付记录暂未同步到列表，请稍后重试。'
    }

    if (FINAL_STATUSES.includes(status.value)) {
      clearTimer()
      return
    }

    if (pollCount.value >= MAX_POLL_COUNT) {
      status.value = 'timeout'
      if (!statusMessage.value) {
        statusMessage.value = '未在预期时间内拿到最终结果。'
      }
      clearTimer()
      return
    }

    clearTimer()
    timer = setTimeout(() => {
      void pollStatus()
    }, 2500)
  } finally {
    polling.value = false
  }
}

function clearTimer() {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
}

async function pollNow() {
  clearTimer()
  await pollStatus()
}

function openPaymentList() {
  openAppPage('/pages/payment/list')
}

function openWaybills() {
  openAppPage('/pages/waybill/list')
}
</script>

<style scoped lang="scss">
.result-card {
  margin-bottom: 24rpx;
}

.result-lines {
  margin-top: 20rpx;
}
</style>
