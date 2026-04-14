<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">Waybill Management</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          Initial admin page for shipment registration, transfer routing and customer-visible tracking events.
        </p>
      </div>
      <div class="flex gap-3">
        <el-button :loading="loading" @click="loadWaybills">Refresh</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">New Waybill</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="mb-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_220px]">
        <el-input v-model="keyword" clearable placeholder="Search by tracking no, reference no or customer" />
        <el-select v-model="statusFilter" clearable placeholder="Filter by status">
          <el-option label="All" value="" />
          <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </div>

      <el-table :data="waybills" v-loading="loading" border class="overflow-hidden rounded-2xl">
        <el-table-column prop="mainTrackingNo" label="Main Tracking No" min-width="180" />
        <el-table-column prop="customerName" label="Customer" min-width="140" />
        <el-table-column label="Destination" min-width="180">
          <template #default="{ row }">
            {{ row.destinationCountry }}<span v-if="row.destinationCity"> / {{ row.destinationCity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="routeType" label="Route Type" width="120" />
        <el-table-column prop="currentStatus" label="Status" width="150" />
        <el-table-column prop="currentNode" label="Current Node" min-width="180" />
        <el-table-column prop="updatedAt" label="Updated At" width="180" />
        <el-table-column label="Actions" width="180" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" @click="openEditDialog(row.id)">Edit</el-button>
              <el-button v-if="canEdit" size="small" type="danger" plain @click="onDelete(row.id)">Delete</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? 'Edit Waybill' : 'Create Waybill'"
      width="1100px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">Base Info</h3>
              <p class="m-0 mt-1 text-sm text-mist">Master waybill fields used by admin management and public query.</p>
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="Main Tracking No" prop="mainTrackingNo">
              <el-input v-model="form.mainTrackingNo" />
            </el-form-item>
            <el-form-item label="Reference No" prop="referenceNo">
              <el-input v-model="form.referenceNo" />
            </el-form-item>
            <el-form-item label="Customer Name" prop="customerName">
              <el-input v-model="form.customerName" />
            </el-form-item>
            <el-form-item label="Customer Phone" prop="customerPhone">
              <el-input v-model="form.customerPhone" />
            </el-form-item>
            <el-form-item label="Origin Warehouse" prop="originWarehouse">
              <el-input v-model="form.originWarehouse" />
            </el-form-item>
            <el-form-item label="Route Type" prop="routeType">
              <el-select v-model="form.routeType">
                <el-option label="direct" value="direct" />
                <el-option label="transfer" value="transfer" />
              </el-select>
            </el-form-item>
            <el-form-item label="Destination Country" prop="destinationCountry">
              <el-input v-model="form.destinationCountry" />
            </el-form-item>
            <el-form-item label="Destination City" prop="destinationCity">
              <el-input v-model="form.destinationCity" />
            </el-form-item>
            <el-form-item label="Current Status" prop="currentStatus">
              <el-select v-model="form.currentStatus">
                <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="Current Node" prop="currentNode">
              <el-input v-model="form.currentNode" />
            </el-form-item>
            <el-form-item label="Package Count" prop="packageCount">
              <el-input-number v-model="form.packageCount" :min="1" class="!w-full" />
            </el-form-item>
            <el-form-item label="Weight Kg" prop="weightKg">
              <el-input-number v-model="form.weightKg" :min="0" :precision="2" class="!w-full" />
            </el-form-item>
            <el-form-item label="Cargo Description" prop="cargoDescription" class="md:col-span-2">
              <el-input v-model="form.cargoDescription" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="Remark" prop="remark" class="md:col-span-2">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
            </el-form-item>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-base font-bold text-ink">Route Legs</h3>
                <p class="m-0 mt-1 text-sm text-mist">Use legs to model transfer hubs, re-waybill numbers and segment ownership.</p>
              </div>
              <el-button type="primary" plain @click="onAddLeg">Add Leg</el-button>
            </div>
          </template>

          <div class="space-y-4">
            <div v-for="(leg, index) in form.legs" :key="`leg-${index}`" class="rounded-2xl border border-slate-200 p-4">
              <div class="mb-4 flex items-center justify-between">
                <p class="m-0 text-sm font-semibold text-slate-900">Leg {{ index + 1 }}</p>
                <el-button type="danger" plain size="small" @click="onRemoveLeg(index)">Remove</el-button>
              </div>
              <div class="grid gap-4 md:grid-cols-2">
                <el-form-item :label="`Leg ${index + 1} Type`">
                  <el-input v-model="leg.legType" />
                </el-form-item>
                <el-form-item :label="`Leg ${index + 1} Tracking No`">
                  <el-input v-model="leg.trackingNo" />
                </el-form-item>
                <el-form-item :label="`Leg ${index + 1} Carrier`">
                  <el-input v-model="leg.carrierName" />
                </el-form-item>
                <el-form-item :label="`Leg ${index + 1} Status`">
                  <el-input v-model="leg.legStatus" />
                </el-form-item>
                <el-form-item :label="`Leg ${index + 1} From Node`">
                  <el-input v-model="leg.fromNode" />
                </el-form-item>
                <el-form-item :label="`Leg ${index + 1} To Node`">
                  <el-input v-model="leg.toNode" />
                </el-form-item>
                <el-form-item :label="`Leg ${index + 1} Departure Time`">
                  <el-input v-model="leg.departureTime" placeholder="yyyy-MM-dd HH:mm:ss" />
                </el-form-item>
                <el-form-item :label="`Leg ${index + 1} Arrival Time`">
                  <el-input v-model="leg.arrivalTime" placeholder="yyyy-MM-dd HH:mm:ss" />
                </el-form-item>
                <el-form-item :label="`Leg ${index + 1} Remark`" class="md:col-span-2">
                  <el-input v-model="leg.remark" />
                </el-form-item>
              </div>
              <el-checkbox v-model="leg.transferFlag">Transfer leg</el-checkbox>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-base font-bold text-ink">Tracking Events</h3>
                <p class="m-0 mt-1 text-sm text-mist">These events will be used for public tracking output.</p>
              </div>
              <el-button type="primary" plain @click="onAddEvent">Add Event</el-button>
            </div>
          </template>

          <div class="space-y-4">
            <div v-for="(event, index) in form.events" :key="`event-${index}`" class="rounded-2xl border border-slate-200 p-4">
              <div class="mb-4 flex items-center justify-between">
                <p class="m-0 text-sm font-semibold text-slate-900">Event {{ index + 1 }}</p>
                <el-button type="danger" plain size="small" @click="onRemoveEvent(index)">Remove</el-button>
              </div>
              <div class="grid gap-4 md:grid-cols-2">
                <el-form-item :label="`Event ${index + 1} Time`">
                  <el-input v-model="event.eventTime" placeholder="yyyy-MM-dd HH:mm:ss" />
                </el-form-item>
                <el-form-item :label="`Event ${index + 1} Status`">
                  <el-input v-model="event.eventStatus" />
                </el-form-item>
                <el-form-item :label="`Event ${index + 1} Description`" class="md:col-span-2">
                  <el-input v-model="event.eventDescription" />
                </el-form-item>
                <el-form-item :label="`Event ${index + 1} Location`">
                  <el-input v-model="event.eventLocation" />
                </el-form-item>
                <el-form-item :label="`Event ${index + 1} Leg Index`">
                  <el-input-number v-model="event.legId" :min="1" :max="form.legs.length || 1" class="!w-full" />
                </el-form-item>
              </div>
              <el-checkbox v-model="event.visibleToCustomer">Visible to customer</el-checkbox>
            </div>
          </div>
        </el-card>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="saving" @click="onSave">Save</el-button>
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
  createEmptyWaybillEvent,
  createEmptyWaybillLeg,
  createWaybill,
  deleteWaybill,
  fetchWaybill,
  fetchWaybills,
  updateWaybill,
} from '../api/waybill'
import { useAuthStore } from '../stores/auth'
import type { WaybillDetail, WaybillEvent, WaybillLeg, WaybillSavePayload, WaybillSummary } from '../types/waybill'

const statusOptions = [
  'created',
  'received',
  'processing',
  'in_transit',
  'transferred',
  'customs_clearance',
  'delivering',
  'signed',
  'exception',
]

const auth = useAuthStore()
const canEdit = computed(() => auth.hasPermission('waybill:edit'))

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const keyword = ref('')
const statusFilter = ref('')
const waybills = ref<WaybillSummary[]>([])
const formRef = ref<FormInstance>()

const form = reactive<WaybillSavePayload>({
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
})

const rules: FormRules = {
  mainTrackingNo: [{ required: true, message: 'Main tracking no is required', trigger: 'blur' }],
  customerName: [{ required: true, message: 'Customer name is required', trigger: 'blur' }],
  destinationCountry: [{ required: true, message: 'Destination country is required', trigger: 'blur' }],
  routeType: [{ required: true, message: 'Route type is required', trigger: 'change' }],
  currentStatus: [{ required: true, message: 'Current status is required', trigger: 'change' }],
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

function resetForm() {
  Object.assign(form, {
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
  })
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
    ElMessage.error('Each leg requires leg type and tracking no.')
    return false
  }
  if (form.events.some((item) => !item.eventTime.trim() || !item.eventStatus.trim() || !item.eventDescription.trim())) {
    ElMessage.error('Each tracking event requires time, status and description.')
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
      ElMessage.success('Waybill updated.')
    } else {
      await createWaybill(payload)
      ElMessage.success('Waybill created.')
    }
    dialogVisible.value = false
    await loadWaybills()
  } catch {
    ElMessage.error('Failed to save waybill.')
  } finally {
    saving.value = false
  }
}

async function onDelete(id: number) {
  try {
    await ElMessageBox.confirm('Delete this waybill?', 'Delete Waybill', {
      type: 'warning',
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel',
    })
  } catch {
    return
  }

  try {
    await deleteWaybill(id)
    ElMessage.success('Waybill deleted.')
    await loadWaybills()
  } catch {
    ElMessage.error('Failed to delete waybill.')
  }
}

function onDialogClosed() {
  editingId.value = null
  resetForm()
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadWaybills()
})
</script>
