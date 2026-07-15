<template>
  <view class="app-shell">
    <slot />
  </view>
</template>

<script setup lang="ts">
</script>

<style>
page {
  background: #f7f8fa;
  color: #1f2937;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

.container {
  padding: 24rpx;
}

.card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(15, 23, 42, 0.06);
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  margin-bottom: 16rpx;
}

.muted {
  color: #6b7280;
  font-size: 24rpx;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.metric-value {
  font-size: 40rpx;
  font-weight: 800;
  margin-top: 8rpx;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eef2f7;
}

.menu-item:last-child {
  border-bottom: none;
}

.button-primary {
  background: #111827;
  color: white;
  border-radius: 999rpx;
  padding: 20rpx 24rpx;
  text-align: center;
  font-size: 28rpx;
}
</style>
