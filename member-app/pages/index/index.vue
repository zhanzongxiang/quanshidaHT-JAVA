<template>
  <view class="container">
    <view class="card">
      <view class="section-title">会员中心</view>
      <view class="muted">集运订单与库存管理</view>
    </view>

    <view class="grid metric-grid">
      <view class="card">
        <view class="muted">库存包裹</view>
        <view class="metric-value">{{ inventoryCount }}</view>
      </view>
      <view class="card">
        <view class="muted">待支付订单</view>
        <view class="metric-value">{{ unpaidOrderCount }}</view>
      </view>
    </view>

    <view class="card menu-card">
      <view class="section-title">快捷入口</view>
      <view class="menu-list">
        <navigator class="menu-item" url="/pages/prealert/index">
          <text>运单预报</text>
          <text>›</text>
        </navigator>
        <navigator class="menu-item" url="/pages/inventory/index">
          <text>我的库存</text>
          <text>›</text>
        </navigator>
        <navigator class="menu-item" url="/pages/shipment/index">
          <text>集运申请</text>
          <text>›</text>
        </navigator>
        <navigator class="menu-item" url="/pages/orders/index">
          <text>我的订单</text>
          <text>›</text>
        </navigator>
        <navigator class="menu-item" url="/pages/address/index">
          <text>收货地址</text>
          <text>›</text>
        </navigator>
        <navigator class="menu-item" url="/pages/profile/index">
          <text>我的账户</text>
          <text>›</text>
        </navigator>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { fetchInventoryPackages, fetchOrders } from '../../api/member'

const inventoryCount = ref(0)
const unpaidOrderCount = ref(0)

async function loadSummary() {
  try {
    const [packages, orders] = await Promise.all([
      fetchInventoryPackages(),
      fetchOrders(),
    ])
    inventoryCount.value = packages.length
    unpaidOrderCount.value = orders.filter((item) => item.paymentStatus === 'unpaid').length
  } catch {
    inventoryCount.value = 0
    unpaidOrderCount.value = 0
  }
}

loadSummary()
</script>

<style scoped>
.metric-grid,
.menu-card {
  margin-top: 24rpx;
}
</style>
