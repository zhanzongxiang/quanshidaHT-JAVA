<template>
  <view class="container">
    <view class="card">
      <view class="section-title">我的库存</view>
      <view class="muted">{{ packages.length }} 个可处理包裹</view>
    </view>

    <view v-if="loading" class="card list-card">
      <view class="muted">加载中...</view>
    </view>

    <view v-else-if="packages.length === 0" class="card list-card">
      <view class="muted">暂无库存包裹</view>
    </view>

    <view v-else class="package-list">
      <view v-for="item in packages" :key="item.id" class="card package-card">
        <view class="package-head">
          <view>
            <view class="package-no">{{ item.packageNo }}</view>
            <view class="muted">{{ item.trackingNo }}</view>
          </view>
          <view class="status-pill" :class="{ warn: item.issueFlag }">
            {{ item.issueFlag ? '问题件' : statusText(item.packageStatus) }}
          </view>
        </view>
        <view class="package-name">{{ item.goodsName }}</view>
        <view class="meta-row">
          <text>仓库：{{ item.warehouseCode || '-' }}</text>
          <text>重量：{{ item.weightKg || 0 }}kg</text>
        </view>
        <view v-if="item.issueNote" class="issue-note">{{ item.issueNote }}</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { fetchInventoryPackages } from '../../api/member'

const loading = ref(false)
const packages = ref([])

function statusText(status) {
  const map = {
    pending_claim: '待认领',
    in_stock: '已入库',
    issue: '待处理',
  }
  return map[status] || status || '-'
}

async function loadData() {
  loading.value = true
  try {
    packages.value = await fetchInventoryPackages()
  } catch (error) {
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

loadData()
</script>

<style scoped>
.list-card,
.package-list {
  margin-top: 24rpx;
}

.package-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.package-card {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.package-head,
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.package-no {
  font-size: 30rpx;
  font-weight: 800;
}

.package-name {
  font-size: 28rpx;
  color: #111827;
}

.meta-row {
  color: #6b7280;
  font-size: 24rpx;
}

.status-pill {
  flex-shrink: 0;
  border-radius: 999rpx;
  background: #ecfdf5;
  color: #047857;
  font-size: 22rpx;
  padding: 8rpx 18rpx;
}

.status-pill.warn {
  background: #fff7ed;
  color: #c2410c;
}

.issue-note {
  border-radius: 16rpx;
  background: #fff7ed;
  color: #9a3412;
  font-size: 24rpx;
  padding: 16rpx;
}
</style>
