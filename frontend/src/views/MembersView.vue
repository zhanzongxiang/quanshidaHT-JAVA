<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">Members</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          Manage mini-program member accounts, member status, and member-visible waybill bindings.
        </p>
      </div>
      <div class="flex gap-3">
        <el-button :loading="loading" @click="loadData">Refresh</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">New Member</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="mb-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_220px]">
        <el-input v-model="keyword" clearable placeholder="Search by phone, nickname, or name" />
        <el-select v-model="statusFilter" clearable placeholder="Filter by status">
          <el-option label="All Statuses" value="" />
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-table :data="members" v-loading="loading" border class="overflow-hidden rounded-2xl">
        <el-table-column prop="phone" label="Phone" min-width="140" />
        <el-table-column prop="wechatOpenid" label="WeChat OpenID" min-width="180" />
        <el-table-column prop="nickname" label="Nickname" min-width="120" />
        <el-table-column prop="fullName" label="Name" min-width="120" />
        <el-table-column label="Status" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="waybillCount" label="Waybills" width="120" />
        <el-table-column prop="wechatBindTime" label="WeChat Bound At" min-width="180" />
        <el-table-column prop="lastLoginAt" label="Last Login" min-width="180" />
        <el-table-column prop="createdAt" label="Created At" min-width="180" />
        <el-table-column label="Actions" width="260" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" @click="openEditDialog(row.id)">{{ canEdit ? 'Edit' : 'View' }}</el-button>
              <el-button
                v-if="canEdit && row.status !== 'active'"
                size="small"
                type="success"
                plain
                @click="onChangeStatus(row.id, 'active')"
              >
                Enable
              </el-button>
              <el-button
                v-if="canEdit && row.status !== 'disabled'"
                size="small"
                type="danger"
                plain
                @click="onChangeStatus(row.id, 'disabled')"
              >
                Disable
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? 'Edit Member' : 'New Member'"
      width="960px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">Profile</h3>
              <p class="m-0 mt-1 text-sm text-mist">
                The member phone is used for mini-program login and automatic waybill ownership matching.
              </p>
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="Phone" prop="phone">
              <el-input v-model="form.phone" maxlength="11" />
            </el-form-item>
            <el-form-item label="WeChat OpenID">
              <el-input :model-value="currentWechatOpenid" disabled placeholder="Bind from member-side API" />
            </el-form-item>
            <el-form-item label="Password" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                :placeholder="editingId ? 'Leave empty to keep the current password' : 'Enter a password for member login'"
              />
            </el-form-item>
            <el-form-item label="Nickname" prop="nickname">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="Name" prop="fullName">
              <el-input v-model="form.fullName" />
            </el-form-item>
            <el-form-item label="Avatar URL" prop="avatarUrl" class="md:col-span-2">
              <el-input v-model="form.avatarUrl" />
            </el-form-item>
            <el-form-item label="Status" prop="status">
              <el-select v-model="form.status">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="Remark" prop="remark" class="md:col-span-2">
              <el-input v-model="form.remark" type="textarea" :rows="3" />
            </el-form-item>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">Waybill Binding</h3>
              <p class="m-0 mt-1 text-sm text-mist">
                These are manual bindings. The backend also auto-matches waybills by member phone.
              </p>
            </div>
          </template>

          <el-form-item label="Bound Waybills">
            <el-select
              v-model="form.waybillIds"
              multiple
              filterable
              clearable
              collapse-tags
              collapse-tags-tooltip
              class="w-full"
              placeholder="Select waybills to bind"
            >
              <el-option
                v-for="item in waybillOptions"
                :key="item.id"
                :label="`${item.mainTrackingNo} / ${item.customerName}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <div v-if="memberWaybills.length > 0" class="mt-4">
            <p class="mb-3 text-sm font-semibold text-slate-900">Visible Waybills</p>
            <el-table :data="memberWaybills" border size="small" class="overflow-hidden rounded-2xl">
              <el-table-column prop="mainTrackingNo" label="Main Tracking No" min-width="160" />
              <el-table-column prop="customerName" label="Customer" min-width="140" />
              <el-table-column label="Destination" min-width="160">
                <template #default="{ row }">
                  {{ row.destinationCountry }}<span v-if="row.destinationCity"> / {{ row.destinationCity }}</span>
                </template>
              </el-table-column>
              <el-table-column label="Status" width="140">
                <template #default="{ row }">
                  {{ formatWaybillStatus(row.currentStatus) }}
                </template>
              </el-table-column>
              <el-table-column prop="updatedAt" label="Updated At" min-width="180" />
            </el-table>
          </div>
        </el-card>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button v-if="canEdit" type="primary" :loading="saving" @click="onSave">Save</el-button>
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
import { createEmptyMemberPayload, createMember, fetchMember, fetchMembers, updateMember, updateMemberStatus } from '../api/member'
import { fetchWaybills } from '../api/waybill'
import { useAuthStore } from '../stores/auth'
import type { DictionaryOption } from '../types/dictionary'
import type { MemberAdminSavePayload, MemberAdminSummary, MemberWaybillSummary } from '../types/member'
import type { WaybillSummary } from '../types/waybill'

const auth = useAuthStore()
const canEdit = computed(() => auth.hasPermission('member:edit'))

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const keyword = ref('')
const statusFilter = ref('')
const members = ref<MemberAdminSummary[]>([])
const statusOptions = ref<DictionaryOption[]>([])
const waybillStatusOptions = ref<DictionaryOption[]>([])
const waybillOptions = ref<WaybillSummary[]>([])
const memberWaybills = ref<MemberWaybillSummary[]>([])
const currentWechatOpenid = ref('')
const formRef = ref<FormInstance>()

const form = reactive<MemberAdminSavePayload>(createEmptyMemberPayload())

const rules: FormRules = {
  phone: [
    { required: true, message: 'Please enter a phone number', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: 'Phone format is invalid', trigger: 'blur' },
  ],
  status: [{ required: true, message: 'Please select a status', trigger: 'change' }],
}

watch([keyword, statusFilter], () => {
  loadMembers()
})

function resetForm() {
  Object.assign(form, createEmptyMemberPayload())
  memberWaybills.value = []
  currentWechatOpenid.value = ''
}

function toLabelMap(items: DictionaryOption[]) {
  return Object.fromEntries(items.map((item) => [item.value, item.label]))
}

const statusLabelMap = computed(() => toLabelMap(statusOptions.value))
const waybillStatusLabelMap = computed(() => toLabelMap(waybillStatusOptions.value))

function formatStatus(value: string) {
  return statusLabelMap.value[value] ?? value
}

function formatWaybillStatus(value: string) {
  return waybillStatusLabelMap.value[value] ?? value
}

function statusTagType(status: string) {
  if (status === 'active') {
    return 'success'
  }
  if (status === 'disabled') {
    return 'danger'
  }
  return 'warning'
}

async function loadDictionaryData() {
  const data = await fetchDictionaryOptions(['member_status', 'waybill_status'])
  statusOptions.value = data.member_status ?? []
  waybillStatusOptions.value = data.waybill_status ?? []
}

async function loadWaybillOptions() {
  waybillOptions.value = await fetchWaybills()
}

async function loadMembers() {
  loading.value = true
  try {
    members.value = await fetchMembers({
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
    })
  } finally {
    loading.value = false
  }
}

async function loadData() {
  loading.value = true
  try {
    await Promise.all([loadDictionaryData(), loadWaybillOptions(), loadMembers()])
  } catch {
    ElMessage.error('Failed to load member management data.')
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
    const detail = await fetchMember(id)
    editingId.value = id
    Object.assign(form, {
      phone: detail.phone,
      password: '',
      nickname: detail.nickname,
      fullName: detail.fullName,
      avatarUrl: detail.avatarUrl,
      status: detail.status,
      remark: detail.remark,
      waybillIds: [...detail.boundWaybillIds],
    })
    currentWechatOpenid.value = detail.wechatOpenid
    memberWaybills.value = detail.waybills
    dialogVisible.value = true
  } catch {
    ElMessage.error('Failed to load member detail.')
  } finally {
    loading.value = false
  }
}

async function onSave() {
  const valid = formRef.value
    ? await formRef.value.validate().then(() => true).catch(() => false)
    : true

  if (!valid) {
    return
  }

  if (!editingId.value && !form.password.trim()) {
    ElMessage.error('Password is required when creating a member.')
    return
  }

  saving.value = true
  try {
    const payload: MemberAdminSavePayload = {
      ...form,
      password: form.password.trim(),
      waybillIds: [...form.waybillIds],
    }

    if (editingId.value) {
      const detail = await updateMember(editingId.value, payload)
      memberWaybills.value = detail.waybills
      ElMessage.success('Member updated successfully.')
    } else {
      await createMember(payload)
      ElMessage.success('Member created successfully.')
    }
    dialogVisible.value = false
    await loadMembers()
  } catch {
    ElMessage.error('Failed to save member.')
  } finally {
    saving.value = false
  }
}

async function onChangeStatus(id: number, status: string) {
  const actionText = status === 'active' ? 'Enable' : 'Disable'
  try {
    await ElMessageBox.confirm(`Are you sure you want to ${actionText.toLowerCase()} this member?`, `${actionText} Member`, {
      type: 'warning',
      confirmButtonText: actionText,
      cancelButtonText: 'Cancel',
    })
  } catch {
    return
  }

  try {
    await updateMemberStatus(id, status)
    ElMessage.success(`Member ${actionText.toLowerCase()}d successfully.`)
    await loadMembers()
  } catch {
    ElMessage.error(`Failed to ${actionText.toLowerCase()} member.`)
  }
}

function onDialogClosed() {
  editingId.value = null
  resetForm()
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadData()
})
</script>
