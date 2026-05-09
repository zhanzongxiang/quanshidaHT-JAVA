<template>
  <view class="page">
    <view class="card header-card">
      <text class="section-title">我的运单</text>
      <text class="section-subtitle">展示当前会员可见的运单列表，后续可直接从详情进入支付流程。</text>
    </view>

    <view v-if="loading" class="card">
      <text class="empty-text">运单加载中...</text>
    </view>

    <view v-else-if="!waybills.length" class="card">
      <text class="empty-text">当前没有可见运单</text>
    </view>

    <view v-else class="list-stack">
      <view v-for="item in waybills" :key="item.id" class="card list-item" @click="openDetail(item.id)">
        <text class="item-title">{{ item.mainTrackingNo }}</text>
        <text class="item-line">{{ item.customerName }} · {{ item.destinationCountry }} {{ item.destinationCity }}</text>
        <text class="item-line">状态：{{ item.currentStatus }} / {{ item.currentNode || '暂无节点' }}</text>
        <text class="item-line muted">更新时间：{{ item.updatedAt }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchMemberWaybills } from '@/api/member'
import type { MemberWaybillSummary } from '@/types/member'
import { ensureMemberSession } from '@/utils/guards'

const loading = ref(false)
const waybills = ref<MemberWaybillSummary[]>([])

onShow(async () => {
  const ready = await ensureMemberSession()
  if (!ready) {
    return
  }
  await loadWaybills()
})

async function loadWaybills() {
  loading.value = true
  try {
    waybills.value = await fetchMemberWaybills()
  } finally {
    loading.value = false
  }
}

function openDetail(id: number) {
  uni.navigateTo({
    url: `/pages/waybill/detail?id=${id}`,
  })
}
</script>

<style scoped lang="scss">
.header-card {
  margin-bottom: 24rpx;
}

.list-stack {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.list-item {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.item-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1d2f28;
}

.item-line {
  color: #4f544c;
  line-height: 1.6;
}

.muted {
  color: #7b756b;
}
</style>
