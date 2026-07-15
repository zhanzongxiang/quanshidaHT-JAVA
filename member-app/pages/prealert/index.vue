<template>
  <view class="container">
    <view class="card">
      <view class="section-title">运单预报</view>
      <view class="form">
        <input v-model="form.trackingNo" class="input" placeholder="快递单号" />
        <input v-model="form.courierCode" class="input" placeholder="承运商代码" />
        <input v-model="form.warehouseCode" class="input" placeholder="入库仓库" />
        <input v-model="form.goodsName" class="input" placeholder="货物名称" />
        <input v-model.number="form.packageCount" class="input" type="number" placeholder="包裹数量" />
        <input v-model.number="form.estimatedWeight" class="input" type="digit" placeholder="预估重量 kg" />
        <textarea v-model="form.remark" class="textarea" placeholder="备注" />
        <view class="button-primary" @click="submitPrealert">提交预报</view>
      </view>
    </view>

    <view class="card list-card">
      <view class="section-title">预报记录</view>
      <view v-if="prealerts.length === 0" class="muted">暂无预报记录</view>
      <view v-else class="list">
        <view v-for="item in prealerts" :key="item.id" class="row">
          <view>
            <view class="row-title">{{ item.prealertNo }}</view>
            <view class="muted">{{ item.trackingNo }} · {{ item.goodsName }}</view>
          </view>
          <view class="status-pill">{{ statusText(item.status) }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { createPrealert, fetchPrealerts } from '../../api/member'

const prealerts = ref([])
const form = reactive({
  trackingNo: '',
  courierCode: '',
  warehouseCode: '',
  goodsName: '',
  packageCount: 1,
  estimatedWeight: null,
  remark: '',
})

function statusText(status) {
  const map = {
    pending: '待入库',
    matched: '已匹配',
    cancelled: '已取消',
  }
  return map[status] || status || '-'
}

function resetForm() {
  form.trackingNo = ''
  form.courierCode = ''
  form.warehouseCode = ''
  form.goodsName = ''
  form.packageCount = 1
  form.estimatedWeight = null
  form.remark = ''
}

async function loadPrealerts() {
  try {
    prealerts.value = await fetchPrealerts()
  } catch (error) {
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  }
}

async function submitPrealert() {
  if (!form.trackingNo.trim() || !form.goodsName.trim()) {
    uni.showToast({ title: '请填写快递单号和货物名称', icon: 'none' })
    return
  }
  try {
    await createPrealert({ ...form })
    uni.showToast({ title: '提交成功', icon: 'success' })
    resetForm()
    await loadPrealerts()
  } catch (error) {
    uni.showToast({ title: error.message || '提交失败', icon: 'none' })
  }
}

loadPrealerts()
</script>

<style scoped>
.form,
.list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.input,
.textarea {
  box-sizing: border-box;
  border-radius: 18rpx;
  background: #f9fafb;
  color: #111827;
  font-size: 26rpx;
  padding: 0 20rpx;
  width: 100%;
}

.input {
  height: 82rpx;
}

.textarea {
  min-height: 140rpx;
  padding: 20rpx;
}

.list-card {
  margin-top: 24rpx;
}

.row {
  align-items: center;
  border-bottom: 1rpx solid #eef2f7;
  display: flex;
  justify-content: space-between;
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

.status-pill {
  border-radius: 999rpx;
  background: #ecfdf5;
  color: #047857;
  flex-shrink: 0;
  font-size: 22rpx;
  padding: 8rpx 18rpx;
}
</style>
