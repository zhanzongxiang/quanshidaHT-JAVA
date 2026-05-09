<template>
  <view class="page">
    <view class="card header-card">
      <text class="section-title">支付记录</text>
      <text class="section-subtitle">用于联调支付准备、支付完成、退款后的状态变化。</text>
    </view>

    <view v-if="loading" class="card">
      <text class="empty-text">支付记录加载中...</text>
    </view>

    <view v-else-if="!payments.length" class="card">
      <text class="empty-text">当前还没有支付记录</text>
    </view>

    <view v-else class="stack">
      <view v-for="item in payments" :key="item.id" class="card">
        <text class="item-title">{{ item.orderNo }}</text>
        <text class="item-line">商户：{{ item.merchantName }}</text>
        <text class="item-line">运单：{{ item.waybillTrackingNo || '未关联运单' }}</text>
        <text class="item-line">金额：{{ item.amountTotal }} / 实付 {{ item.amountPaid }}</text>
        <text class="item-line">状态：{{ item.status }}</text>
        <text class="item-line">支付时间：{{ item.paidAt || '未支付' }}</text>
        <text class="item-line muted">创建时间：{{ item.createdAt }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchMemberPayments } from '@/api/payment'
import type { MemberPayOrderSummary } from '@/types/payment'
import { ensureMemberSession } from '@/utils/guards'

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
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.header-card {
  margin-bottom: 24rpx;
}

.stack {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.item-title {
  display: block;
  margin-bottom: 12rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #1d2f28;
}

.item-line {
  display: block;
  margin-top: 8rpx;
  color: #4f544c;
  line-height: 1.6;
}

.muted {
  color: #7b756b;
}
</style>
