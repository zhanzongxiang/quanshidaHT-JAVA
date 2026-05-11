<template>
  <view class="page">
    <view v-if="loading" class="card">
      <text class="empty-text">详情加载中...</text>
    </view>

    <template v-else-if="detail">
      <view class="card section">
        <text class="section-title">{{ detail.mainTrackingNo }}</text>
        <text class="section-subtitle">{{ detail.customerName }} · {{ detail.destinationCountry }} {{ detail.destinationCity }}</text>
        <text class="detail-line">状态：{{ detail.currentStatus }}</text>
        <text class="detail-line">当前节点：{{ detail.currentNode || '暂无' }}</text>
        <text class="detail-line">起始仓：{{ detail.originWarehouse || '暂无' }}</text>
        <text class="detail-line">货物：{{ detail.cargoDescription || '暂无' }}</text>
        <text class="detail-line">包裹数：{{ detail.packageCount }}</text>
        <text class="detail-line">重量：{{ detail.weightKg }} kg</text>
      </view>

      <view class="card section">
        <text class="section-title">支付入口</text>
        <text class="section-subtitle">这里对接 `payments/prepare`，用于联调小程序支付链路。</text>
        <view class="field-stack top-gap">
          <view>
            <text class="field-label">支付金额</text>
            <input v-model.trim="payAmount" class="input" type="digit" placeholder="例如 88.50" />
          </view>
          <view>
            <text class="field-label">支付描述</text>
            <input v-model.trim="payDescription" class="input" placeholder="例如 Waybill payment" />
          </view>
        </view>
        <view class="actions top-gap">
          <button class="button-primary" :loading="paying" @click="submitPayment">发起支付</button>
          <button class="button-secondary" @click="openPaymentList">查看支付记录</button>
        </view>
      </view>

      <view class="card section">
        <text class="section-title">分段信息</text>
        <view v-if="detail.legs.length" class="stack">
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
        <view v-if="detail.events.length" class="stack">
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
import { ensureMemberSession } from '@/utils/guards'

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
    uni.showToast({
      title: '缺少运单 id',
      icon: 'none',
    })
    return
  }

  await loadDetail(id)
})

async function loadDetail(id: number) {
  loading.value = true
  try {
    detail.value = await fetchMemberWaybillDetail(id)
  } finally {
    loading.value = false
  }
}

async function submitPayment() {
  if (!detail.value) {
    return
  }
  if (!payAmount.value) {
    uni.showToast({
      title: '请先输入支付金额',
      icon: 'none',
    })
    return
  }

  paying.value = true
  try {
    const payload = await prepareMemberPayment({
      waybillId: detail.value.id,
      amountTotal: Number(payAmount.value),
      description: payDescription.value || `Waybill payment ${detail.value.mainTrackingNo}`,
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

    uni.navigateTo({
      url: `/pages/payment/result?orderNo=${encodeURIComponent(payload.orderNo)}&status=${encodeURIComponent(payload.status)}`,
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '支付发起失败'
    const normalized = message.toLowerCase()
    const status = normalized.includes('cancel') ? 'closed' : 'exception'
    uni.navigateTo({
      url: `/pages/payment/result?orderNo=&status=${encodeURIComponent(status)}&message=${encodeURIComponent(message)}`,
    })
  } finally {
    paying.value = false
  }
}

function openPaymentList() {
  uni.switchTab({
    url: '/pages/payment/list',
  })
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

.stack {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
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
