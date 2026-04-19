<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">运单管理</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          用于维护主运单、中转线路和客户可见轨迹事件的后台管理页面。
        </p>
      </div>
      <div class="flex gap-3">
        <el-button :loading="loading" @click="loadWaybills">刷新</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">新建运单</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="mb-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_220px]">
        <el-input v-model="keyword" clearable placeholder="按主运单号、参考单号或客户名称搜索" />
        <el-select v-model="statusFilter" clearable placeholder="按状态筛选">
          <el-option label="全部状态" value="" />
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-table :data="waybills" v-loading="loading" border class="overflow-hidden rounded-2xl">
        <el-table-column prop="mainTrackingNo" label="主运单号" min-width="180" />
        <el-table-column prop="customerName" label="客户名称" min-width="140" />
        <el-table-column label="目的地" min-width="180">
          <template #default="{ row }">
            {{ row.destinationCountry }}<span v-if="row.destinationCity"> / {{ row.destinationCity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="线路类型" width="120">
          <template #default="{ row }">
            {{ formatRouteType(row.routeType) }}
          </template>
        </el-table-column>
        <el-table-column label="当前状态" width="150">
          <template #default="{ row }">
            {{ formatStatus(row.currentStatus) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentNode" label="当前节点" min-width="180" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" @click="openEditDialog(row.id)">编辑</el-button>
              <el-button v-if="canEdit" size="small" type="danger" plain @click="onDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑运单' : '新建运单'"
      width="1100px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">基础信息</h3>
              <p class="m-0 mt-1 text-sm text-mist">维护后台管理和前台公开查询使用的主运单核心字段。</p>
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="主运单号" prop="mainTrackingNo">
              <el-input v-model="form.mainTrackingNo" />
            </el-form-item>
            <el-form-item label="参考单号" prop="referenceNo">
              <el-input v-model="form.referenceNo" />
            </el-form-item>
            <el-form-item label="客户名称" prop="customerName">
              <el-input v-model="form.customerName" />
            </el-form-item>
            <el-form-item label="客户电话" prop="customerPhone">
              <el-input v-model="form.customerPhone" />
            </el-form-item>
            <el-form-item label="始发仓" prop="originWarehouse">
              <el-input v-model="form.originWarehouse" />
            </el-form-item>
            <el-form-item label="线路类型" prop="routeType">
              <el-select v-model="form.routeType">
                <el-option v-for="item in routeTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="目的国家" prop="destinationCountry">
              <el-input v-model="form.destinationCountry" />
            </el-form-item>
            <el-form-item label="目的城市" prop="destinationCity">
              <el-input v-model="form.destinationCity" />
            </el-form-item>
            <el-form-item label="当前状态" prop="currentStatus">
              <el-select v-model="form.currentStatus">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="当前节点" prop="currentNode">
              <el-input v-model="form.currentNode" />
            </el-form-item>
            <el-form-item label="包裹数量" prop="packageCount">
              <el-input-number v-model="form.packageCount" :min="1" class="!w-full" />
            </el-form-item>
            <el-form-item label="重量（KG）" prop="weightKg">
              <el-input-number v-model="form.weightKg" :min="0" :precision="2" class="!w-full" />
            </el-form-item>
            <el-form-item label="货物描述" prop="cargoDescription" class="md:col-span-2">
              <el-input v-model="form.cargoDescription" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="备注" prop="remark" class="md:col-span-2">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
            </el-form-item>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-base font-bold text-ink">线路分段</h3>
                <p class="m-0 mt-1 text-sm text-mist">用于维护中转节点、分段运单号和承运归属。</p>
              </div>
              <el-button type="primary" plain @click="onAddLeg">新增分段</el-button>
            </div>
          </template>

          <div class="space-y-4">
            <div v-for="(leg, index) in form.legs" :key="`leg-${index}`" class="rounded-2xl border border-slate-200 p-4">
              <div class="mb-4 flex items-center justify-between">
                <p class="m-0 text-sm font-semibold text-slate-900">分段 {{ index + 1 }}</p>
                <el-button type="danger" plain size="small" @click="onRemoveLeg(index)">删除分段</el-button>
              </div>
              <div class="grid gap-4 md:grid-cols-2">
                <el-form-item :label="`分段 ${index + 1} 类型`">
                  <el-input v-model="leg.legType" />
                </el-form-item>
                <el-form-item :label="`分段 ${index + 1} 运单号`">
                  <el-input v-model="leg.trackingNo" />
                </el-form-item>
                <el-form-item :label="`分段 ${index + 1} 承运商`">
                  <el-input v-model="leg.carrierName" />
                </el-form-item>
                <el-form-item :label="`分段 ${index + 1} 状态`">
                  <el-select v-model="leg.legStatus">
                    <el-option v-for="item in legStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="`分段 ${index + 1} 起始节点`">
                  <el-input v-model="leg.fromNode" />
                </el-form-item>
                <el-form-item :label="`分段 ${index + 1} 目标节点`">
                  <el-input v-model="leg.toNode" />
                </el-form-item>
                <el-form-item :label="`分段 ${index + 1} 发出时间`">
                  <el-input v-model="leg.departureTime" placeholder="yyyy-MM-dd HH:mm:ss" />
                </el-form-item>
                <el-form-item :label="`分段 ${index + 1} 到达时间`">
                  <el-input v-model="leg.arrivalTime" placeholder="yyyy-MM-dd HH:mm:ss" />
                </el-form-item>
                <el-form-item :label="`分段 ${index + 1} 备注`" class="md:col-span-2">
                  <el-input v-model="leg.remark" />
                </el-form-item>
              </div>
              <el-checkbox v-model="leg.transferFlag">中转分段</el-checkbox>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-base font-bold text-ink">轨迹事件</h3>
                <p class="m-0 mt-1 text-sm text-mist">这些事件会作为前台运单查询的轨迹输出。</p>
              </div>
              <el-button type="primary" plain @click="onAddEvent">新增事件</el-button>
            </div>
          </template>

          <div class="space-y-4">
            <div v-for="(event, index) in form.events" :key="`event-${index}`" class="rounded-2xl border border-slate-200 p-4">
              <div class="mb-4 flex items-center justify-between">
                <p class="m-0 text-sm font-semibold text-slate-900">事件 {{ index + 1 }}</p>
                <el-button type="danger" plain size="small" @click="onRemoveEvent(index)">删除事件</el-button>
              </div>
              <div class="grid gap-4 md:grid-cols-2">
                <el-form-item :label="`事件 ${index + 1} 时间`">
                  <el-input v-model="event.eventTime" placeholder="yyyy-MM-dd HH:mm:ss" />
                </el-form-item>
                <el-form-item :label="`事件 ${index + 1} 状态`">
                  <el-select v-model="event.eventStatus">
                    <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="`事件 ${index + 1} 描述`" class="md:col-span-2">
                  <el-input v-model="event.eventDescription" />
                </el-form-item>
                <el-form-item :label="`事件 ${index + 1} 位置`">
                  <el-input v-model="event.eventLocation" />
                </el-form-item>
                <el-form-item :label="`事件 ${index + 1} 所属分段`">
                  <el-input-number v-model="event.legId" :min="1" :max="form.legs.length || 1" class="!w-full" />
                </el-form-item>
              </div>
              <el-checkbox v-model="event.visibleToCustomer">对客户可见</el-checkbox>
            </div>
          </div>
        </el-card>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { fetchDictionaryOptions } from '../api/dictionary'
import {
  createEmptyWaybillEvent,
  createEmptyWaybillLeg,
  createWaybill,
  deleteWaybill,
  fetchWaybill,
  fetchWaybills,
  updateWaybill,
} from '../api/waybill'
import { useAuthStore } from '../stores/auth'
import type { DictionaryOption } from '../types/dictionary'
import type { WaybillDetail, WaybillEvent, WaybillLeg, WaybillSavePayload, WaybillSummary } from '../types/waybill'

const auth = useAuthStore()
const canEdit = computed(() => auth.hasPermission('waybill:edit'))

const routeTypeOptions = ref<DictionaryOption[]>([])
const statusOptions = ref<DictionaryOption[]>([])
const legStatusOptions = ref<DictionaryOption[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const keyword = ref('')
const statusFilter = ref('')
const waybills = ref<WaybillSummary[]>([])
const formRef = ref<FormInstance>()

function createInitialWaybillForm(): WaybillSavePayload {
  return {
    mainTrackingNo: '',
    referenceNo: '',
    customerName: '',
    customerPhone: '',
    originWarehouse: '',
    destinationCountry: '',
    destinationCity: '',
    routeType: 'direct',
    currentStatus: 'created',
    currentNode: '',
    cargoDescription: '',
    packageCount: 1,
    weightKg: null,
    remark: '',
    legs: [createEmptyWaybillLeg(1)],
    events: [createEmptyWaybillEvent(1)],
  }
}

const form = reactive<WaybillSavePayload>(createInitialWaybillForm())

const rules: FormRules = {
  mainTrackingNo: [{ required: true, message: '请输入主运单号', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  destinationCountry: [{ required: true, message: '请输入目的国家', trigger: 'blur' }],
  routeType: [{ required: true, message: '请选择线路类型', trigger: 'change' }],
  currentStatus: [{ required: true, message: '请选择当前状态', trigger: 'change' }],
}

watch([keyword, statusFilter], () => {
  loadWaybills()
})

function cloneLegs(legs: WaybillLeg[]): WaybillLeg[] {
  return legs.map((item) => ({ ...item }))
}

function cloneEvents(events: WaybillEvent[]): WaybillEvent[] {
  return events.map((item) => ({ ...item }))
}

function toLabelMap(items: DictionaryOption[]) {
  return Object.fromEntries(items.map((item) => [item.value, item.label]))
}

const routeTypeLabelMap = computed(() => toLabelMap(routeTypeOptions.value))
const statusLabelMap = computed(() => toLabelMap(statusOptions.value))

function formatRouteType(value: string) {
  return routeTypeLabelMap.value[value] ?? value
}

function formatStatus(value: string) {
  return statusLabelMap.value[value] ?? value
}

async function loadDictionaryData() {
  const data = await fetchDictionaryOptions([
    'waybill_route_type',
    'waybill_status',
    'waybill_leg_status',
  ])

  routeTypeOptions.value = data.waybill_route_type ?? []
  statusOptions.value = data.waybill_status ?? []
  legStatusOptions.value = data.waybill_leg_status ?? []
}

function resetForm() {
  Object.assign(form, createInitialWaybillForm())
}

async function loadWaybills() {
  loading.value = true
  try {
    waybills.value = await fetchWaybills({
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
    })
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(id: number) {
  loading.value = true
  try {
    const waybill: WaybillDetail = await fetchWaybill(id)
    editingId.value = id
    Object.assign(form, {
      mainTrackingNo: waybill.mainTrackingNo,
      referenceNo: waybill.referenceNo,
      customerName: waybill.customerName,
      customerPhone: waybill.customerPhone,
      originWarehouse: waybill.originWarehouse,
      destinationCountry: waybill.destinationCountry,
      destinationCity: waybill.destinationCity,
      routeType: waybill.routeType,
      currentStatus: waybill.currentStatus,
      currentNode: waybill.currentNode,
      cargoDescription: waybill.cargoDescription,
      packageCount: waybill.packageCount,
      weightKg: waybill.weightKg,
      remark: waybill.remark,
      legs: cloneLegs(waybill.legs),
      events: cloneEvents(waybill.events),
    })
    dialogVisible.value = true
  } finally {
    loading.value = false
  }
}

function onAddLeg() {
  form.legs.push(createEmptyWaybillLeg(form.legs.length + 1))
}

function onRemoveLeg(index: number) {
  form.legs.splice(index, 1)
  if (form.legs.length === 0) {
    form.legs.push(createEmptyWaybillLeg(1))
  }
  form.legs.forEach((item, legIndex) => {
    item.legNo = legIndex + 1
  })
}

function onAddEvent() {
  form.events.push(createEmptyWaybillEvent(form.events.length + 1))
}

function onRemoveEvent(index: number) {
  form.events.splice(index, 1)
  if (form.events.length === 0) {
    form.events.push(createEmptyWaybillEvent(1))
  }
  form.events.forEach((item, eventIndex) => {
    item.sortNo = eventIndex + 1
  })
}

function validateDetails() {
  if (form.legs.some((item) => !item.legType.trim() || !item.trackingNo.trim())) {
    ElMessage.error('每个分段都必须填写分段类型和运单号。')
    return false
  }
  if (form.events.some((item) => !item.eventTime.trim() || !item.eventStatus.trim() || !item.eventDescription.trim())) {
    ElMessage.error('每个轨迹事件都必须填写时间、状态和描述。')
    return false
  }
  return true
}

async function onSave() {
  const valid = formRef.value
    ? await formRef.value.validate().then(() => true).catch(() => false)
    : true

  if (!valid || !validateDetails()) {
    return
  }

  saving.value = true
  try {
    const payload: WaybillSavePayload = {
      ...form,
      legs: cloneLegs(form.legs),
      events: cloneEvents(form.events),
    }

    if (editingId.value) {
      await updateWaybill(editingId.value, payload)
      ElMessage.success('运单更新成功。')
    } else {
      await createWaybill(payload)
      ElMessage.success('运单创建成功。')
    }
    dialogVisible.value = false
    await loadWaybills()
  } catch {
    ElMessage.error('运单保存失败。')
  } finally {
    saving.value = false
  }
}

async function onDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除这条运单吗？', '删除运单', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    await deleteWaybill(id)
    ElMessage.success('运单删除成功。')
    await loadWaybills()
  } catch {
    ElMessage.error('运单删除失败。')
  }
}

function onDialogClosed() {
  editingId.value = null
  resetForm()
  formRef.value?.clearValidate()
}

onMounted(() => {
  Promise.all([loadDictionaryData(), loadWaybills()]).catch(() => {
    ElMessage.error('运单字典加载失败。')
  })
})
</script>
