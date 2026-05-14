<template>
  <view class="page">
    <view v-if="loading" class="card">
      <text class="empty-text">详情加载中...</text>
    </view>

    <template v-else-if="detail">
      <view class="card section">
        <text class="section-title">{{ detail.mainTrackingNo }}</text>
        <text class="section-subtitle">{{ detail.customerName }} · {{ detail.destinationCountry }} {{ detail.destinationCity || '' }}</text>
        <text class="detail-line">状态：{{ formatWaybillStatus(detail.currentStatus) }}</text>
        <text class="detail-line">当前节点：{{ detail.currentNode || '暂无' }}</text>
        <text class="detail-line">始发仓：{{ detail.originWarehouse || '暂无' }}</text>
        <text class="detail-line">货物描述：{{ detail.cargoDescription || '暂无' }}</text>
        <text class="detail-line">包裹数量：{{ detail.packageCount }}</text>
        <text class="detail-line">重量：{{ detail.weightKg ?? 0 }} kg</text>
      </view>

      <view class="card section">
        <text class="section-title">支付入口</text>
        <text class="section-subtitle">这里对接 `payments/prepare`，用于联调小程序支付链路。</text>
        <view class="field-stack top-gap">
          <view class="field-block">
            <text class="field-label">支付金额</text>
            <input
              v-model.trim="payAmount"
              class="input"
              type="digit"
              maxlength="10"
              placeholder="例如 88.50"
            />
          </view>
          <view class="field-block">
            <text class="field-label">支付描述</text>
            <input
              v-model.trim="payDescription"
              class="input"
              maxlength="50"
              placeholder="例如 Waybill payment"
            />
          </view>
        </view>
        <view class="actions top-gap">
          <button class="button-primary" :loading="paying" :disabled="paying" @click="submitPayment">
            {{ paying ? '支付发起中...' : '发起支付' }}
          </button>
          <button class="button-secondary" @click="openPaymentList">查看支付记录</button>
        </view>
      </view>

      <view class="card section">
        <text class="section-title">分段信息</text>
        <view v-if="detail.legs.length" class="stack top-gap">
          <view v-for="(leg, index) in detail.legs" :key="`${leg.trackingNo}-${index}`" class="sub-card">
            <text class="sub-title">第 {{ leg.legNo || index + 1 }} 段 · {{ leg.legType }}</text>
            <text class="detail-line">运单号：{{ leg.trackingNo }}</text>
            <text class="detail-line">承运商：{{ leg.carrierName || '暂无' }}</text>
            <text class="detail-line">路线：{{ leg.fromNode || '起点待补' }} -> {{ leg.toNode || '终点待补' }}</text>
            <text class="detail-line">状态：{{ leg.legStatus || 'pending' }}</text>
          </view>
        </view>
        <text v-else class="empty-text">暂无分段信息</text>
      </view>

      <view class="card section">
        <text class="section-title">轨迹事件</text>
        <view v-if="detail.events.length" class="stack top-gap">
          <view v-for="(event, index) in detail.events" :key="`${event.eventTime}-${index}`" class="sub-card">
            <text class="sub-title">{{ event.eventStatus }}</text>
            <text class="detail-line">{{ event.eventDescription }}</text>
            <text class="detail-line">时间：{{ event.eventTime }}</text>
            <text class="detail-line">地点：{{ event.eventLocation || '暂无' }}</text>
          </view>
        </view>
        <text v-else class="empty-text">暂无轨迹事件</text>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchMemberWaybillDetail } from '@/api/member'
import { prepareMemberPayment } from '@/api/payment'
import type { MemberWaybillDetail } from '@/types/member'
import { formatWaybillStatus } from '@/utils/display'
import { ensureMemberSession } from '@/utils/guards'
import { openAppPage } from '@/utils/navigation'
import { showError } from '@/utils/toast'
import { parsePaymentAmount } from '@/utils/validation'
import { normalizePaymentResultMessage } from '@/utils/wechat'

const loading = ref(false)
const paying = ref(false)
const detail = ref<MemberWaybillDetail | null>(null)
const payAmount = ref('')
const payDescription = ref('Waybill payment')

onLoad(async (query) => {
  const ready = await ensureMemberSession()
  if (!ready) {
    return
  }

  const id = Number(query?.id || 0)
  if (!id) {
    showError('缺少运单 ID')
    return
  }

  await loadDetail(id)
})

async function loadDetail(id: number) {
  loading.value = true
  try {
    detail.value = await fetchMemberWaybillDetail(id)
  } catch (error) {
    showError(error, '运单详情加载失败')
  } finally {
    loading.value = false
  }
}

async function submitPayment() {
  if (!detail.value || paying.value) {
    return
  }

  const amountTotal = parsePaymentAmount(payAmount.value)
  if (amountTotal === null) {
    showError('请输入正确的支付金额，最多保留两位小数')
    return
  }

  paying.value = true
  try {
    const payload = await prepareMemberPayment({
      waybillId: detail.value.id,
      amountTotal,
      description: payDescription.value.trim() || `Waybill payment ${detail.value.mainTrackingNo}`,
      channel: 'wechat_pay',
    })

    await uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payload.timeStamp,
      nonceStr: payload.nonceStr,
      package: payload.packageValue,
      signType: payload.signType,
      paySign: payload.paySign,
    })

      openAppPage(`/pages/payment/result?orderNo=${encodeURIComponent(payload.orderNo)}&status=${encodeURIComponent(payload.status)}`)
  } catch (error) {
    const result = normalizePaymentResultMessage(error)
      openAppPage(`/pages/payment/result?orderNo=&status=${encodeURIComponent(result.status)}&message=${encodeURIComponent(result.message)}`)
  } finally {
    paying.value = false
  }
}

function openPaymentList() {
  openAppPage('/pages/payment/list')
}
</script>

<style scoped lang="scss">
.section {
  margin-bottom: 24rpx;
}

.sub-card {
  padding: 20rpx;
  border-radius: 20rpx;
  background: #faf5ea;
}

.sub-title {
  display: block;
  margin-bottom: 10rpx;
  font-weight: 700;
  color: #1f2f29;
}
</style>
