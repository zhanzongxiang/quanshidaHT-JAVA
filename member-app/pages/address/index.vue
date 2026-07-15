<template>
  <view class="container">
    <view class="card">
      <view class="section-title">收货地址</view>
      <view class="form">
        <input v-model="form.contactName" class="input" placeholder="收货人" />
        <input v-model="form.contactPhone" class="input" placeholder="手机号" />
        <input v-model="form.province" class="input" placeholder="省份" />
        <input v-model="form.city" class="input" placeholder="城市" />
        <input v-model="form.detailAddress" class="input" placeholder="详细地址" />
        <label class="checkbox-row">
          <checkbox :checked="form.isDefault" @click="form.isDefault = !form.isDefault" />
          <text>设为默认地址</text>
        </label>
        <view class="button-primary" @click="submitAddress">保存地址</view>
      </view>
    </view>

    <view class="card list-card">
      <view class="section-title">地址列表</view>
      <view v-if="addresses.length === 0" class="muted">暂无地址</view>
      <view v-else class="address-list">
        <view v-for="item in addresses" :key="item.id" class="address-row">
          <view>
            <view class="name">{{ item.contactName }} {{ item.contactPhone }}</view>
            <view class="muted">{{ item.province }}{{ item.city }}{{ item.district }}{{ item.detailAddress }}</view>
          </view>
          <view v-if="item.isDefault" class="default-pill">默认</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { createAddress, fetchAddresses } from '../../api/member'

const addresses = ref([])
const form = reactive({
  contactName: '',
  contactPhone: '',
  country: '中国',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  isDefault: false,
})

function resetForm() {
  form.contactName = ''
  form.contactPhone = ''
  form.province = ''
  form.city = ''
  form.district = ''
  form.detailAddress = ''
  form.postalCode = ''
  form.isDefault = false
}

async function loadAddresses() {
  try {
    addresses.value = await fetchAddresses()
  } catch (error) {
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  }
}

async function submitAddress() {
  if (!form.contactName.trim() || !form.contactPhone.trim() || !form.detailAddress.trim()) {
    uni.showToast({ title: '请填写收货人、手机号和详细地址', icon: 'none' })
    return
  }
  try {
    await createAddress({ ...form })
    uni.showToast({ title: '保存成功', icon: 'success' })
    resetForm()
    await loadAddresses()
  } catch (error) {
    uni.showToast({ title: error.message || '保存失败', icon: 'none' })
  }
}

loadAddresses()
</script>

<style scoped>
.form,
.address-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.input {
  box-sizing: border-box;
  border-radius: 18rpx;
  background: #f9fafb;
  color: #111827;
  font-size: 26rpx;
  height: 82rpx;
  padding: 0 20rpx;
  width: 100%;
}

.checkbox-row {
  align-items: center;
  color: #374151;
  display: flex;
  font-size: 26rpx;
  gap: 12rpx;
}

.list-card {
  margin-top: 24rpx;
}

.address-row {
  align-items: flex-start;
  border-bottom: 1rpx solid #eef2f7;
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  padding: 18rpx 0;
}

.address-row:last-child {
  border-bottom: none;
}

.name {
  color: #111827;
  font-size: 28rpx;
  font-weight: 800;
}

.default-pill {
  border-radius: 999rpx;
  background: #ecfdf5;
  color: #047857;
  flex-shrink: 0;
  font-size: 22rpx;
  padding: 8rpx 18rpx;
}
</style>
