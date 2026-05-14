<template>
  <view class="page">
    <view class="card header-card">
      <text class="section-title">支付记录</text>
      <text class="section-subtitle">用于联调支付准备、支付完成、关闭、退款后的状态变化。</text>
      <view class="actions top-gap">
        <button class="button-secondary" :loading="loading" @click="loadPayments">刷新记录</button>
      </view>
    </view>

    <view v-if="loading" class="card">
      <text class="empty-text">支付记录加载中...</text>
    </view>

    <view v-else-if="!payments.length" class="card">
      <text class="empty-text">当前还没有支付记录</text>
    </view>

    <view v-else class="stack">
      <view v-for="item in payments" :key="item.id" class="card">
        <view class="row-between">
          <text class="item-title">{{ item.orderNo }}</text>
          <text class="pill">{{ formatPaymentStatus(item.status) }}</text>
        </view>
        <text class="detail-line">商户：{{ item.merchantName }}</text>
        <text class="detail-line">运单：{{ item.waybillTrackingNo || '未关联运单' }}</text>
        <text class="detail-line">金额：{{ item.amountTotal }} / 实付 {{ item.amountPaid }}</text>
        <text class="detail-line">说明：{{ item.description || '暂无' }}</text>
        <text class="detail-line">支付时间：{{ item.paidAt || '未支付' }}</text>
        <text class="detail-line muted">创建时间：{{ item.createdAt }}</text>
        <view class="actions top-gap">
          <button class="button-primary" @click="openResult(item)">查看结果</button>
          <button v-if="item.waybillId" class="button-secondary" @click="openWaybill(item.waybillId)">查看运单</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchMemberPayments } from '@/api/payment'
import type { MemberPayOrderSummary } from '@/types/payment'
import { formatPaymentStatus } from '@/utils/display'
import { ensureMemberSession } from '@/utils/guards'
import { openAppPage } from '@/utils/navigation'
import { showError } from '@/utils/toast'

const loading = ref(false)
const payments = ref<MemberPayOrderSummary[]>([])

onShow(async () => {
  const ready = await ensureMemberSession()
  if (!ready) {
    return
  }
  await loadPayments()
})

async function loadPayments() {
  loading.value = true
  try {
    payments.value = await fetchMemberPayments()
  } catch (error) {
    showError(error, '支付记录加载失败')
  } finally {
    loading.value = false
  }
}

function openResult(item: MemberPayOrderSummary) {
  openAppPage(`/pages/payment/result?orderNo=${encodeURIComponent(item.orderNo)}&status=${encodeURIComponent(item.status)}`)
}

function openWaybill(waybillId: number) {
  openAppPage(`/pages/waybill/detail?id=${waybillId}`)
}
</script>

<style scoped lang="scss">
.header-card {
  margin-bottom: 24rpx;
}

.item-title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #1d2f28;
  word-break: break-all;
}
</style>
