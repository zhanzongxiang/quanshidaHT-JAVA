<template>
  <view class="container">
    <view class="card">
      <view class="section-title">集运申请</view>
      <view class="muted">已选择 {{ selectedPackageIds.length }} 个包裹</view>
    </view>

    <view class="card block-card">
      <view class="block-title">收货地址</view>
      <picker :range="addressOptions" range-key="label" @change="onAddressChange">
        <view class="picker-field">{{ selectedAddressLabel || '请选择收货地址' }}</view>
      </picker>
    </view>

    <view class="card block-card">
      <view class="block-title">可集运包裹</view>
      <view v-if="loading" class="muted">加载中...</view>
      <view v-else-if="packages.length === 0" class="muted">暂无可集运包裹</view>
      <view v-else class="package-list">
        <view
          v-for="item in packages"
          :key="item.id"
          class="package-row"
          :class="{ active: selectedPackageIds.includes(item.id), disabled: !canSelectPackage(item) }"
          @click="togglePackage(item.id)"
        >
          <view>
            <view class="package-no">{{ item.packageNo }}</view>
            <view class="muted">{{ item.goodsName }}</view>
          </view>
          <view class="weight">{{ item.weightKg || 0 }}kg</view>
        </view>
      </view>
    </view>

    <view class="card block-card">
      <view class="block-title">备注</view>
      <textarea v-model="remark" class="textarea" placeholder="可填写打包要求" />
      <view class="button-primary submit-button" @click="submitShipment">提交集运申请</view>
    </view>

    <view class="card block-card">
      <view class="block-title">我的集运单</view>
      <view v-if="shipments.length === 0" class="muted">暂无集运单</view>
      <view v-else class="shipment-list">
        <view v-for="item in shipments" :key="item.id" class="shipment-row">
          <view>
            <view class="package-no">{{ item.shipmentNo }}</view>
            <view class="muted">{{ item.createdAt }}</view>
          </view>
          <view class="status-pill">{{ shipmentStatusText(item.shipmentStatus) }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { createShipment, fetchAddresses, fetchInventoryPackages, fetchShipments } from '../../api/member'

const loading = ref(false)
const packages = ref([])
const addresses = ref([])
const shipments = ref([])
const selectedAddressId = ref(null)
const selectedPackageIds = ref([])
const remark = ref('')

const addressOptions = computed(() => addresses.value.map((item) => ({
  id: item.id,
  label: `${item.contactName} ${item.contactPhone} ${item.province || ''}${item.city || ''}${item.detailAddress || ''}`,
})))

const selectedAddressLabel = computed(() => {
  const item = addressOptions.value.find((option) => option.id === selectedAddressId.value)
  return item?.label || ''
})

function shipmentStatusText(status) {
  const map = {
    submitted: '已提交',
    quoted: '已核价',
    paid: '已支付',
    outbound: '已出库',
    completed: '已完成',
    cancelled: '已取消',
  }
  return map[status] || status || '-'
}

function onAddressChange(event) {
  const index = Number(event.detail.value)
  selectedAddressId.value = addressOptions.value[index]?.id || null
}

function canSelectPackage(item) {
  return !item.issueFlag && ['pending_claim', 'in_stock'].includes(item.packageStatus)
}

function togglePackage(id) {
  const item = packages.value.find((packageItem) => packageItem.id === id)
  if (!item || !canSelectPackage(item)) {
    uni.showToast({ title: '异常或不可集运包裹需先处理', icon: 'none' })
    return
  }
  if (selectedPackageIds.value.includes(id)) {
    selectedPackageIds.value = selectedPackageIds.value.filter((item) => item !== id)
    return
  }
  selectedPackageIds.value = [...selectedPackageIds.value, id]
}

async function loadData() {
  loading.value = true
  try {
    const [packageData, addressData, shipmentData] = await Promise.all([
      fetchInventoryPackages(),
      fetchAddresses(),
      fetchShipments(),
    ])
    packages.value = packageData
    addresses.value = addressData
    shipments.value = shipmentData
    if (!selectedAddressId.value) {
      const defaultAddress = addressData.find((item) => item.isDefault) || addressData[0]
      selectedAddressId.value = defaultAddress?.id || null
    }
  } catch (error) {
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

async function submitShipment() {
  if (!selectedAddressId.value) {
    uni.showToast({ title: '请选择收货地址', icon: 'none' })
    return
  }
  if (selectedPackageIds.value.length === 0) {
    uni.showToast({ title: '请选择包裹', icon: 'none' })
    return
  }
  try {
    await createShipment({
      addressId: selectedAddressId.value,
      packageIds: selectedPackageIds.value,
      remark: remark.value,
    })
    uni.showToast({ title: '提交成功', icon: 'success' })
    selectedPackageIds.value = []
    remark.value = ''
    await loadData()
  } catch (error) {
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
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

.picker-field,
.textarea {
  border-radius: 18rpx;
  background: #f9fafb;
  color: #111827;
  font-size: 26rpx;
  padding: 18rpx;
}

.textarea {
  box-sizing: border-box;
  min-height: 150rpx;
  width: 100%;
}

.package-list,
.shipment-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.package-row,
.shipment-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1rpx solid #eef2f7;
  border-radius: 18rpx;
  gap: 16rpx;
  padding: 18rpx;
}

.package-row.active {
  border-color: #111827;
  background: #f9fafb;
}

.package-row.disabled {
  opacity: 0.55;
}

.package-no {
  color: #111827;
  font-size: 28rpx;
  font-weight: 800;
}

.weight {
  color: #111827;
  font-size: 26rpx;
  font-weight: 700;
}

.status-pill {
  border-radius: 999rpx;
  background: #ecfdf5;
  color: #047857;
  font-size: 22rpx;
  padding: 8rpx 18rpx;
}

.submit-button {
  margin-top: 20rpx;
}
</style>
