<template>
  <view class="container">
    <view class="card">
      <view class="section-title">我的订单</view>
      <view class="muted">{{ orders.length }} 条订单，{{ financeRecords.length }} 条账务记录</view>
    </view>

    <view class="card block-card">
      <view class="block-title">订单列表</view>
      <view v-if="loading" class="muted">加载中...</view>
      <view v-else-if="orders.length === 0" class="muted">暂无订单</view>
      <view v-else class="list">
        <view v-for="item in orders" :key="item.id" class="row">
          <view>
            <view class="row-title">{{ item.orderNo }}</view>
            <view class="muted">{{ item.createdAt }}</view>
          </view>
          <view class="amount">
            {{ item.amount || 0 }} {{ item.currencyCode || 'CNY' }}
            <view class="status">{{ paymentStatusText(item.paymentStatus) }}</view>
          </view>
        </view>
      </view>
    </view>

    <view class="card block-card">
      <view class="block-title">账务记录</view>
      <view v-if="financeRecords.length === 0" class="muted">暂无账务记录</view>
      <view v-else class="list">
        <view v-for="item in financeRecords" :key="item.id" class="row">
          <view>
            <view class="row-title">{{ item.recordNo }}</view>
            <view class="muted">{{ item.note || item.createdAt }}</view>
          </view>
          <view class="amount">
            {{ item.amount || 0 }} {{ item.currencyCode || 'CNY' }}
            <view class="status">{{ recordStatusText(item.recordStatus) }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { fetchFinanceRecords, fetchOrders } from '../../api/member'

const loading = ref(false)
const orders = ref([])
const financeRecords = ref([])

function paymentStatusText(status) {
  const map = {
    unpaid: '待支付',
    paid: '已支付',
    refunded: '已退款',
  }
  return map[status] || status || '-'
}

function recordStatusText(status) {
  const map = {
    pending: '待确认',
    confirmed: '已确认',
    cancelled: '已取消',
  }
  return map[status] || status || '-'
}

async function loadData() {
  loading.value = true
  try {
    const [orderData, financeData] = await Promise.all([
      fetchOrders(),
      fetchFinanceRecords(),
    ])
    orders.value = orderData
    financeRecords.value = financeData
  } catch (error) {
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

loadData()
</script>

<style scoped>
.block-card {
  margin-top: 24rpx;
}

.block-title {
  font-size: 28rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid #eef2f7;
  gap: 16rpx;
  padding: 18rpx 0;
}

.row:last-child {
  border-bottom: none;
}

.row-title {
  color: #111827;
  font-size: 28rpx;
  font-weight: 800;
}

.amount {
  color: #111827;
  font-size: 26rpx;
  font-weight: 800;
  text-align: right;
}

.status {
  color: #6b7280;
  font-size: 22rpx;
  font-weight: 400;
  margin-top: 6rpx;
}
</style>
