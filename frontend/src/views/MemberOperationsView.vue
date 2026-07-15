<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 class="m-0 text-xl font-extrabold text-ink">会员物流运营</h2>
          <p class="m-0 mt-2 max-w-3xl text-sm leading-6 text-mist">
            处理会员预报入库、在库包裹、集运核价和订单收款，让会员端提交后的流程能在后台闭环。
          </p>
        </div>
        <el-button :loading="loading" @click="loadActive">刷新</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="mb-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_220px]">
        <el-input v-model="keyword" clearable placeholder="按会员号、用户名、手机号或单号搜索" />
        <el-select v-model="statusFilter" clearable placeholder="按状态筛选">
          <el-option label="全部状态" value="" />
          <el-option v-for="item in currentStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-tabs v-model="activeTab" class="member-operation-tabs">
        <el-tab-pane label="包裹预报" name="prealerts">
          <el-table :data="prealerts" v-loading="loading" border class="overflow-hidden rounded-2xl">
            <el-table-column label="会员" min-width="150">
              <template #default="{ row }">{{ formatMember(row.memberNo, row.username) }}</template>
            </el-table-column>
            <el-table-column prop="prealertNo" label="预报号" min-width="180" />
            <el-table-column prop="trackingNo" label="快递单号" min-width="180" />
            <el-table-column prop="goodsName" label="货物名称" min-width="160" />
            <el-table-column prop="warehouseCode" label="仓库" width="100" />
            <el-table-column prop="packageCount" label="件数" width="90" />
            <el-table-column label="预估重量" width="120">
              <template #default="{ row }">{{ formatWeight(row.estimatedWeight) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="prealertTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="提交时间" width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button v-if="canOperate && row.status !== 'matched'" size="small" type="primary" @click="openInboundDialog(row)">
                  入库
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="在库包裹" name="packages">
          <el-table :data="packages" v-loading="loading" border class="overflow-hidden rounded-2xl">
            <el-table-column label="会员" min-width="150">
              <template #default="{ row }">{{ formatMember(row.memberNo, row.username) }}</template>
            </el-table-column>
            <el-table-column prop="packageNo" label="包裹号" min-width="180" />
            <el-table-column prop="trackingNo" label="快递单号" min-width="180" />
            <el-table-column prop="goodsName" label="货物名称" min-width="160" />
            <el-table-column prop="warehouseCode" label="仓库" width="100" />
            <el-table-column prop="packageCount" label="件数" width="90" />
            <el-table-column label="重量" width="110">
              <template #default="{ row }">{{ formatWeight(row.weightKg) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="140">
              <template #default="{ row }">
                <el-tag :type="packageTagType(row.packageStatus)">{{ formatStatus(row.packageStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="异常" width="90">
              <template #default="{ row }">
                <el-tag :type="row.issueFlag ? 'danger' : 'info'">{{ row.issueFlag ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warehouseInAt" label="入库时间" width="180" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="集运单" name="shipments">
          <el-table :data="shipments" v-loading="loading" border class="overflow-hidden rounded-2xl">
            <el-table-column label="会员" min-width="150">
              <template #default="{ row }">{{ formatMember(row.memberNo, row.username) }}</template>
            </el-table-column>
            <el-table-column prop="shipmentNo" label="集运单号" min-width="180" />
            <el-table-column label="包裹" width="100">
              <template #default="{ row }">{{ row.packageCount }} 件</template>
            </el-table-column>
            <el-table-column label="总重量" width="120">
              <template #default="{ row }">{{ formatWeight(row.totalWeight) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="140">
              <template #default="{ row }">
                <el-tag :type="shipmentTagType(row.shipmentStatus)">{{ formatStatus(row.shipmentStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="180" />
            <el-table-column prop="createdAt" label="提交时间" width="180" />
            <el-table-column label="操作" width="230" fixed="right">
              <template #default="{ row }">
                <div class="flex flex-wrap gap-2">
                  <el-button
                    v-if="canOperate && ['submitted', 'quoted'].includes(row.shipmentStatus)"
                    size="small"
                    type="primary"
                    @click="openQuoteDialog(row)"
                  >
                    核价
                  </el-button>
                    <el-dropdown v-if="canOperate" @command="(status: string) => onUpdateShipmentStatus(row.id, status)">
                      <el-button size="small">改状态</el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                        <el-dropdown-item v-for="item in manualShipmentStatusOptions" :key="item.value" :command="item.value">
                          {{ item.label }}
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="订单收款" name="orders">
          <el-table :data="orders" v-loading="loading" border class="overflow-hidden rounded-2xl">
            <el-table-column label="会员" min-width="150">
              <template #default="{ row }">{{ formatMember(row.memberNo, row.username) }}</template>
            </el-table-column>
            <el-table-column prop="orderNo" label="订单号" min-width="180" />
            <el-table-column prop="shipmentId" label="集运 ID" width="100" />
            <el-table-column label="金额" width="140">
              <template #default="{ row }">{{ row.currencyCode }} {{ formatAmount(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="订单状态" width="130">
              <template #default="{ row }">
                <el-tag :type="orderTagType(row.orderStatus)">{{ formatStatus(row.orderStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="支付状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.paymentStatus === 'paid' ? 'success' : 'warning'">{{ formatStatus(row.paymentStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="180" />
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button v-if="canOperate && row.paymentStatus !== 'paid'" size="small" type="primary" @click="onMarkOrderPaid(row)">
                  确认收款
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="inboundDialogVisible" title="预报入库" width="560px" destroy-on-close @closed="resetInboundForm">
      <el-form ref="inboundFormRef" :model="inboundForm" :rules="inboundRules" label-position="top">
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input v-model="inboundForm.warehouseCode" />
        </el-form-item>
        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="包裹件数" prop="packageCount">
            <el-input-number v-model="inboundForm.packageCount" :min="1" class="!w-full" />
          </el-form-item>
          <el-form-item label="实重（KG）" prop="weightKg">
            <el-input-number v-model="inboundForm.weightKg" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </div>
        <el-form-item label="入库备注" prop="remark">
          <el-input v-model="inboundForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="inboundDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onInbound">确认入库</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="quoteDialogVisible" title="集运核价" width="520px" destroy-on-close @closed="resetQuoteForm">
      <el-form ref="quoteFormRef" :model="quoteForm" :rules="quoteRules" label-position="top">
        <el-form-item label="应收金额" prop="amount">
          <el-input-number v-model="quoteForm.amount" :min="0" :precision="2" class="!w-full" />
        </el-form-item>
        <el-form-item label="核价备注" prop="remark">
          <el-input v-model="quoteForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="quoteDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onQuote">保存核价</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  fetchAdminOrders,
  fetchAdminPackages,
  fetchAdminPrealerts,
  fetchAdminShipments,
  inboundAdminPrealert,
  markAdminOrderPaid,
  quoteAdminShipment,
  updateAdminShipmentStatus,
} from '../api/member-operations'
import { useAuthStore } from '../stores/auth'
import type {
  AdminMemberOrder,
  AdminMemberPackage,
  AdminMemberPrealert,
  AdminMemberShipment,
} from '../types/member-operations'

type ActiveTab = 'prealerts' | 'packages' | 'shipments' | 'orders'
type StatusOption = { label: string; value: string }
type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

const auth = useAuthStore()
const canOperate = computed(() => auth.hasPermission('member:operate'))

const activeTab = ref<ActiveTab>('prealerts')
const keyword = ref('')
const statusFilter = ref('')
const loading = ref(false)
const saving = ref(false)
const prealerts = ref<AdminMemberPrealert[]>([])
const packages = ref<AdminMemberPackage[]>([])
const shipments = ref<AdminMemberShipment[]>([])
const orders = ref<AdminMemberOrder[]>([])

const inboundDialogVisible = ref(false)
const quoteDialogVisible = ref(false)
const selectedPrealert = ref<AdminMemberPrealert | null>(null)
const selectedShipment = ref<AdminMemberShipment | null>(null)
const inboundFormRef = ref<FormInstance>()
const quoteFormRef = ref<FormInstance>()

const inboundForm = reactive({
  warehouseCode: '',
  packageCount: 1 as number | null,
  weightKg: null as number | null,
  remark: '',
})

const quoteForm = reactive({
  amount: 0,
  remark: '',
})

const prealertStatusOptions: StatusOption[] = [
  { label: '待处理', value: 'pending' },
  { label: '已入库', value: 'matched' },
  { label: '已取消', value: 'cancelled' },
]

const packageStatusOptions: StatusOption[] = [
  { label: '待认领', value: 'pending_claim' },
  { label: '在库', value: 'in_stock' },
  { label: '异常', value: 'issue' },
  { label: '已提交集运', value: 'shipment_submitted' },
  { label: '已出库', value: 'outbound' },
  { label: '已完成', value: 'completed' },
]

const shipmentStatusOptions: StatusOption[] = [
  { label: '已提交', value: 'submitted' },
  { label: '已核价', value: 'quoted' },
  { label: '已支付', value: 'paid' },
  { label: '已出库', value: 'outbound' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

const manualShipmentStatusOptions: StatusOption[] = [
  { label: '已出库', value: 'outbound' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

const orderStatusOptions: StatusOption[] = [
  { label: '待支付', value: 'pending_payment' },
  { label: '已支付', value: 'paid' },
  { label: '已取消', value: 'cancelled' },
]

const statusLabelMap = computed<Record<string, string>>(() => {
  const options = [
    ...prealertStatusOptions,
    ...packageStatusOptions,
    ...shipmentStatusOptions,
    ...orderStatusOptions,
    { label: '未支付', value: 'unpaid' },
  ]
  return Object.fromEntries(options.map((item) => [item.value, item.label]))
})

const currentStatusOptions = computed(() => {
  if (activeTab.value === 'prealerts') {
    return prealertStatusOptions
  }
  if (activeTab.value === 'packages') {
    return packageStatusOptions
  }
  if (activeTab.value === 'shipments') {
    return shipmentStatusOptions
  }
  return orderStatusOptions
})

const inboundRules: FormRules = {
  warehouseCode: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
  packageCount: [{ required: true, message: '请输入包裹件数', trigger: 'change' }],
}

const quoteRules: FormRules = {
  amount: [{ required: true, message: '请输入应收金额', trigger: 'change' }],
}

watch(activeTab, () => {
  statusFilter.value = ''
  loadActive()
})

watch([keyword, statusFilter], () => {
  loadActive()
})

function formatMember(memberNo: string, username: string) {
  return `${memberNo || '-'} / ${username || '-'}`
}

function formatWeight(value: number | null) {
  return value == null ? '-' : `${Number(value).toFixed(2)} KG`
}

function formatAmount(value: number) {
  return Number(value || 0).toFixed(2)
}

function formatStatus(value: string) {
  return statusLabelMap.value[value] ?? value
}

function prealertTagType(status: string): TagType {
  if (status === 'matched') {
    return 'success'
  }
  if (status === 'cancelled') {
    return 'info'
  }
  return 'warning'
}

function packageTagType(status: string): TagType {
  if (status === 'issue') {
    return 'danger'
  }
  if (status === 'in_stock') {
    return 'success'
  }
  if (status === 'completed') {
    return 'info'
  }
  return 'warning'
}

function shipmentTagType(status: string): TagType {
  if (status === 'completed') {
    return 'success'
  }
  if (status === 'cancelled') {
    return 'info'
  }
  if (status === 'outbound') {
    return 'primary'
  }
  return 'warning'
}

function orderTagType(status: string): TagType {
  if (status === 'paid') {
    return 'success'
  }
  if (status === 'cancelled') {
    return 'info'
  }
  return 'warning'
}

function queryParams() {
  return {
    keyword: keyword.value || undefined,
    status: statusFilter.value || undefined,
  }
}

async function loadActive() {
  loading.value = true
  try {
    if (activeTab.value === 'prealerts') {
      prealerts.value = await fetchAdminPrealerts(queryParams())
    } else if (activeTab.value === 'packages') {
      packages.value = await fetchAdminPackages(queryParams())
    } else if (activeTab.value === 'shipments') {
      shipments.value = await fetchAdminShipments(queryParams())
    } else {
      orders.value = await fetchAdminOrders(queryParams())
    }
  } catch {
    ElMessage.error('会员物流数据加载失败。')
  } finally {
    loading.value = false
  }
}

function resetInboundForm() {
  selectedPrealert.value = null
  Object.assign(inboundForm, {
    warehouseCode: '',
    packageCount: 1,
    weightKg: null,
    remark: '',
  })
  inboundFormRef.value?.clearValidate()
}

function openInboundDialog(row: AdminMemberPrealert) {
  selectedPrealert.value = row
  inboundForm.warehouseCode = row.warehouseCode || ''
  inboundForm.packageCount = row.packageCount || 1
  inboundForm.weightKg = row.estimatedWeight
  inboundForm.remark = row.remark || ''
  inboundDialogVisible.value = true
}

async function onInbound() {
  const valid = inboundFormRef.value
    ? await inboundFormRef.value.validate().then(() => true).catch(() => false)
    : true
  if (!valid || !selectedPrealert.value) {
    return
  }

  saving.value = true
  try {
    await inboundAdminPrealert(selectedPrealert.value.id, { ...inboundForm })
    ElMessage.success('包裹入库成功。')
    inboundDialogVisible.value = false
    await loadActive()
  } catch {
    ElMessage.error('包裹入库失败。')
  } finally {
    saving.value = false
  }
}

function resetQuoteForm() {
  selectedShipment.value = null
  Object.assign(quoteForm, {
    amount: 0,
    remark: '',
  })
  quoteFormRef.value?.clearValidate()
}

function openQuoteDialog(row: AdminMemberShipment) {
  selectedShipment.value = row
  quoteForm.amount = 0
  quoteForm.remark = row.remark || ''
  quoteDialogVisible.value = true
}

async function onQuote() {
  const valid = quoteFormRef.value
    ? await quoteFormRef.value.validate().then(() => true).catch(() => false)
    : true
  if (!valid || !selectedShipment.value) {
    return
  }

  saving.value = true
  try {
    await quoteAdminShipment(selectedShipment.value.id, { ...quoteForm })
    ElMessage.success('集运核价成功。')
    quoteDialogVisible.value = false
    await loadActive()
  } catch {
    ElMessage.error('集运核价失败。')
  } finally {
    saving.value = false
  }
}

async function onUpdateShipmentStatus(id: number, status: string) {
  saving.value = true
  try {
    await updateAdminShipmentStatus(id, status)
    ElMessage.success('集运状态已更新。')
    await loadActive()
  } catch {
    ElMessage.error('集运状态更新失败。')
  } finally {
    saving.value = false
  }
}

async function onMarkOrderPaid(row: AdminMemberOrder) {
  try {
    await ElMessageBox.confirm(`确认订单 ${row.orderNo} 已收款吗？`, '确认收款', {
      type: 'warning',
      confirmButtonText: '确认收款',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  saving.value = true
  try {
    await markAdminOrderPaid(row.id)
    ElMessage.success('订单收款已确认。')
    await loadActive()
  } catch {
    ElMessage.error('确认收款失败。')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadActive()
})
</script>
