<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">会员管理</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          维护小程序会员账号、状态和可见运单范围，保证后台与会员端数据一致。
        </p>
      </div>
      <div class="flex gap-3">
        <el-button :loading="loading" @click="loadData">刷新</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">新建会员</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="mb-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_220px]">
        <el-input v-model="keyword" clearable placeholder="按手机号、昵称或姓名搜索" />
        <el-select v-model="statusFilter" clearable placeholder="按状态筛选">
          <el-option label="全部状态" value="" />
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-table :data="members" v-loading="loading" border class="overflow-hidden rounded-2xl">
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="fullName" label="姓名" min-width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="微信绑定" min-width="180">
          <template #default="{ row }">
            <span v-if="row.wechatOpenid">{{ maskIdentifier(row.wechatOpenid) }}</span>
            <span v-else class="text-mist">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column prop="waybillCount" label="可见运单数" width="120" />
        <el-table-column prop="wechatBindTime" label="微信绑定时间" min-width="180" />
        <el-table-column prop="lastLoginAt" label="最近登录" min-width="180" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" @click="openEditDialog(row.id)">{{ canEdit ? '编辑' : '查看' }}</el-button>
              <el-button
                v-if="canEdit && row.status !== 'active'"
                size="small"
                type="success"
                plain
                @click="onChangeStatus(row.id, 'active')"
              >
                启用
              </el-button>
              <el-button
                v-if="canEdit && row.status !== 'disabled'"
                size="small"
                type="danger"
                plain
                @click="onChangeStatus(row.id, 'disabled')"
              >
                停用
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑会员' : '新建会员'"
      width="960px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" :disabled="!canEdit" label-position="top" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">基础资料</h3>
              <p class="m-0 mt-1 text-sm text-mist">手机号用于会员登录和后台自动关联运单。</p>
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" maxlength="11" placeholder="请输入 11 位手机号" />
            </el-form-item>
            <el-form-item label="微信 OpenID">
              <el-input :model-value="currentWechatOpenid || ''" disabled placeholder="由会员登录或绑定后回填" />
            </el-form-item>
            <el-form-item label="登录密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                :placeholder="editingId ? '留空表示不修改当前密码' : '请输入登录密码'"
              />
            </el-form-item>
            <el-form-item label="会员状态" prop="status">
              <el-select v-model="form.status">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" maxlength="64" />
            </el-form-item>
            <el-form-item label="姓名" prop="fullName">
              <el-input v-model="form.fullName" maxlength="64" />
            </el-form-item>
            <el-form-item label="头像地址" prop="avatarUrl" class="md:col-span-2">
              <el-input v-model="form.avatarUrl" maxlength="500" placeholder="可留空" />
            </el-form-item>
            <el-form-item label="备注" prop="remark" class="md:col-span-2">
              <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">运单绑定</h3>
              <p class="m-0 mt-1 text-sm text-mist">支持手工绑定运单，后台仍会按手机号自动补充可见运单。</p>
            </div>
          </template>

          <el-form-item label="手工绑定运单">
            <el-select
              v-model="form.waybillIds"
              multiple
              filterable
              clearable
              collapse-tags
              collapse-tags-tooltip
              class="w-full"
              placeholder="请选择需要绑定的运单"
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
            <p class="mb-3 text-sm font-semibold text-slate-900">当前可见运单</p>
            <el-table :data="memberWaybills" border size="small" class="overflow-hidden rounded-2xl">
              <el-table-column prop="mainTrackingNo" label="主运单号" min-width="160" />
              <el-table-column prop="customerName" label="客户名称" min-width="140" />
              <el-table-column label="目的地" min-width="180">
                <template #default="{ row }">
                  {{ row.destinationCountry }}<span v-if="row.destinationCity"> / {{ row.destinationCity }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="140">
                <template #default="{ row }">
                  {{ formatWaybillStatus(row.currentStatus) }}
                </template>
              </el-table-column>
              <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
            </el-table>
          </div>
          <el-empty v-else description="当前没有可见运单" />
        </el-card>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">{{ canEdit ? '取消' : '关闭' }}</el-button>
          <el-button v-if="canEdit" type="primary" :loading="saving" @click="onSave">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { fetchDictionaryOptions } from '../api/dictionary'
import { createEmptyMemberPayload, createMember, fetchMember, fetchMembers, updateMember, updateMemberStatus } from '../api/member'
import { fetchWaybills } from '../api/waybill'
import { useAuthStore } from '../stores/auth'
import type { DictionaryOption } from '../types/dictionary'
import type { MemberAdminSavePayload, MemberAdminSummary, MemberWaybillSummary } from '../types/member'
import type { WaybillSummary } from '../types/waybill'
import { runSafely, runWithLoading } from '../utils/async'
import { confirmAction, showErrorMessage, showSuccessMessage } from '../utils/message'
import { isValidPhone, normalizePhone } from '../utils/validation'

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
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  status: [{ required: true, message: '请选择会员状态', trigger: 'change' }],
}

watch([keyword, statusFilter], () => {
  void loadMembers()
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

function maskIdentifier(value: string) {
  if (!value) {
    return ''
  }
  if (value.length <= 10) {
    return value
  }
  return `${value.slice(0, 4)}...${value.slice(-4)}`
}

async function loadMembers() {
  const data = await runWithLoading(
    loading,
    () =>
      fetchMembers({
        keyword: keyword.value || undefined,
        status: statusFilter.value || undefined,
      }),
    '会员列表加载失败',
  )

  if (data) {
    members.value = data
  }
}

async function loadData() {
  const data = await runWithLoading(
    loading,
    async () => {
      const [dictionaryData, waybillData, memberData] = await Promise.all([
        fetchDictionaryOptions(['member_status', 'waybill_status']),
        fetchWaybills(),
        fetchMembers({
          keyword: keyword.value || undefined,
          status: statusFilter.value || undefined,
        }),
      ])

      return { dictionaryData, waybillData, memberData }
    },
    '会员管理数据加载失败',
  )

  if (!data) {
    return
  }

  statusOptions.value = data.dictionaryData.member_status ?? []
  waybillStatusOptions.value = data.dictionaryData.waybill_status ?? []
  waybillOptions.value = data.waybillData
  members.value = data.memberData
}

function openCreateDialog() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(id: number) {
  const detail = await runWithLoading(loading, () => fetchMember(id), '会员详情加载失败')
  if (!detail) {
    return
  }

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
}

function buildPayload(): MemberAdminSavePayload {
  return {
    ...form,
    phone: normalizePhone(form.phone),
    password: form.password.trim(),
    nickname: form.nickname.trim(),
    fullName: form.fullName.trim(),
    avatarUrl: form.avatarUrl.trim(),
    remark: form.remark.trim(),
    waybillIds: [...new Set(form.waybillIds.filter((id) => Number.isInteger(id) && id > 0))],
  }
}

async function onSave() {
  const valid = formRef.value
    ? await formRef.value.validate().then(() => true).catch(() => false)
    : true

  if (!valid) {
    return
  }

  const payload = buildPayload()
  if (!isValidPhone(payload.phone)) {
    showErrorMessage('请输入正确的 11 位手机号')
    return
  }

  if (!editingId.value && !payload.password) {
    showErrorMessage('新建会员时必须填写登录密码')
    return
  }

  saving.value = true
  try {
    if (editingId.value) {
      const detail = await updateMember(editingId.value, payload)
      memberWaybills.value = detail.waybills
      currentWechatOpenid.value = detail.wechatOpenid
      showSuccessMessage('会员更新成功')
    } else {
      await createMember(payload)
      showSuccessMessage('会员创建成功')
    }
    dialogVisible.value = false
    await loadMembers()
  } catch (error) {
    showErrorMessage(error, '会员保存失败')
  } finally {
    saving.value = false
  }
}

async function onChangeStatus(id: number, status: string) {
  const actionText = status === 'active' ? '启用' : '停用'
  const confirmed = await confirmAction(`确认${actionText}该会员吗？`, `${actionText}会员`, actionText)
  if (!confirmed) {
    return
  }

  await runSafely(async () => {
    await updateMemberStatus(id, status)
    showSuccessMessage(`会员${actionText}成功`)
    await loadMembers()
  }, `会员${actionText}失败`)
}

function onDialogClosed() {
  editingId.value = null
  resetForm()
  formRef.value?.clearValidate()
}

onMounted(() => {
  void loadData()
})
</script>
