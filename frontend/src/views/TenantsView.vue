<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">租户管理</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          维护租户基础信息、访问域名和启停状态，作为多公司平台治理的基础入口。
        </p>
      </div>
      <div class="flex gap-3">
        <el-button :loading="loading" @click="loadData">刷新</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">新建租户</el-button>
      </div>
    </div>

    <div class="grid gap-4 xl:grid-cols-[360px_minmax(0,1fr)]">
      <el-card class="rounded-3xl border-0 shadow-panel">
        <template #header>
          <div>
            <h3 class="m-0 text-base font-bold text-ink">当前租户上下文</h3>
            <p class="m-0 mt-1 text-sm text-mist">当前登录和请求链路解析出的租户信息。</p>
          </div>
        </template>

        <div v-if="currentTenant" class="space-y-4">
          <div class="rounded-2xl border border-slate-200 bg-white px-4 py-4">
            <div class="text-xs uppercase tracking-[0.2em] text-mist">Tenant Code</div>
            <div class="mt-2 text-lg font-bold text-ink">{{ currentTenant.tenantCode }}</div>
          </div>

          <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">租户名称</div>
              <div class="mt-1 font-semibold text-ink">{{ currentTenant.tenantName }}</div>
            </div>
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">状态</div>
              <div class="mt-1">
                <el-tag :type="statusTagType(currentTenant.status)">{{ formatStatus(currentTenant.status) }}</el-tag>
              </div>
            </div>
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">时区</div>
              <div class="mt-1 font-medium text-ink">{{ currentTenant.timezone }}</div>
            </div>
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">语言</div>
              <div class="mt-1 font-medium text-ink">{{ currentTenant.locale }}</div>
            </div>
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">启用域名数</div>
              <div class="mt-1 font-medium text-ink">{{ enabledDomainCount(currentTenant) }}</div>
            </div>
          </div>

          <div v-if="currentTenant.bootstrapAdmin" class="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-4">
            <div class="text-xs uppercase tracking-[0.2em] text-amber-700">Bootstrap Admin</div>
            <div class="mt-2 text-sm text-slate-700">
              用户名：<span class="font-semibold text-slate-900">{{ currentTenant.bootstrapAdmin.username }}</span>
            </div>
            <div class="mt-1 text-sm text-slate-700">
              初始密码：<span class="font-semibold text-slate-900">{{ currentTenant.bootstrapAdmin.initialPassword }}</span>
            </div>
          </div>

          <div class="rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-4 py-3 text-sm text-mist">
            {{ currentTenant.remark || '当前租户暂无备注。' }}
          </div>
        </div>
      </el-card>

      <el-card class="rounded-3xl border-0 shadow-panel">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">租户列表</h3>
              <p class="m-0 mt-1 text-sm text-mist">超级管理员可维护租户基础信息与访问域名。</p>
            </div>
            <el-tag type="info">{{ tenants.length }} 个租户</el-tag>
          </div>
        </template>

        <el-table :data="tenants" v-loading="loading" border class="overflow-hidden rounded-2xl">
          <el-table-column prop="tenantCode" label="租户编码" min-width="140" />
          <el-table-column prop="tenantName" label="租户名称" min-width="180" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="timezone" label="时区" min-width="150" />
          <el-table-column prop="locale" label="语言" min-width="120" />
          <el-table-column label="域名" min-width="260">
            <template #default="{ row }">
              <div class="flex flex-wrap gap-2">
                <el-tag
                  v-for="domain in row.domains.slice(0, 3)"
                  :key="domain.id"
                  :type="domain.enabled ? 'success' : 'info'"
                  effect="plain"
                >
                  {{ domain.domain }}
                </el-tag>
                <span v-if="row.domains.length === 0" class="text-sm text-mist">未配置</span>
                <span v-else-if="row.domains.length > 3" class="text-sm text-mist">+{{ row.domains.length - 3 }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="初始化管理员" min-width="200">
            <template #default="{ row }">
              <div v-if="row.bootstrapAdmin" class="text-sm text-slate-700">
                <div>{{ row.bootstrapAdmin.username }}</div>
                <div class="text-mist">{{ row.bootstrapAdmin.initialPassword }}</div>
              </div>
              <span v-else class="text-sm text-mist">未初始化</span>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEditDialog(row)">{{ canEdit ? '编辑' : '查看' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingTenantId ? '编辑租户' : '新建租户'"
      width="920px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" :disabled="!canEdit" label-position="top" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">基础信息</h3>
              <p class="m-0 mt-1 text-sm text-mist">租户编码用于上下文识别，停用状态受平台保护规则限制。</p>
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="租户编码" prop="tenantCode">
              <el-input v-model="form.tenantCode" maxlength="64" placeholder="例如：default / acme" />
            </el-form-item>
            <el-form-item label="租户名称" prop="tenantName">
              <el-input v-model="form.tenantName" maxlength="128" placeholder="例如：Acme Logistics" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status">
                <el-option label="启用" value="ACTIVE" />
                <el-option label="停用" value="DISABLED" />
              </el-select>
            </el-form-item>
            <el-form-item label="时区" prop="timezone">
              <el-input v-model="form.timezone" maxlength="64" placeholder="例如：Asia/Shanghai" />
            </el-form-item>
            <el-form-item label="语言" prop="locale">
              <el-input v-model="form.locale" maxlength="32" placeholder="例如：zh-CN" />
            </el-form-item>
            <el-form-item label="备注" prop="remark" class="md:col-span-2">
              <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-base font-bold text-ink">域名管理</h3>
                <p class="m-0 mt-1 text-sm text-mist">支持为租户绑定多个域名，启用域名会参与公共站点租户解析。</p>
              </div>
              <el-button v-if="canEdit" type="primary" plain @click="addDomainRow">新增域名</el-button>
            </div>
          </template>

          <div v-if="form.domains.length > 0" class="space-y-4">
            <div
              v-for="(domain, index) in form.domains"
              :key="`domain-${index}`"
              class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4"
            >
              <div class="grid gap-4 md:grid-cols-[minmax(0,1.6fr)_180px_120px_auto]">
                <el-form-item :label="`域名 ${index + 1}`" :prop="`domains.${index}.domain`" class="mb-0">
                  <el-input v-model="domain.domain" maxlength="255" placeholder="例如：acme.example.com" />
                </el-form-item>
                <el-form-item :label="`类型 ${index + 1}`" :prop="`domains.${index}.domainType`" class="mb-0">
                  <el-select v-model="domain.domainType">
                    <el-option label="website" value="website" />
                    <el-option label="admin" value="admin" />
                    <el-option label="api" value="api" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="`启用 ${index + 1}`" class="mb-0">
                  <el-switch v-model="domain.enabled" />
                </el-form-item>
                <div class="flex items-end justify-end">
                  <el-button v-if="canEdit" type="danger" plain @click="removeDomainRow(index)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="当前未配置域名" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { createEmptyTenantPayload, createTenant, fetchCurrentTenant, fetchTenants, updateTenant } from '../api/tenant'
import { useAuthStore } from '../stores/auth'
import type { TenantDomainPayload, TenantSavePayload, TenantSummary } from '../types/tenant'
import { runSafely, runWithLoading } from '../utils/async'
import { showSuccessMessage } from '../utils/message'

const auth = useAuthStore()
const canEdit = computed(() => auth.hasPermission('tenant:edit'))

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const currentTenant = ref<TenantSummary | null>(null)
const tenants = ref<TenantSummary[]>([])
const editingTenantId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<TenantSavePayload>(createEmptyTenantPayload())

const rules: FormRules = {
  tenantCode: [
    { required: true, message: '请输入租户编码', trigger: 'blur' },
    { min: 2, max: 64, message: '租户编码长度需在 2 到 64 之间', trigger: 'blur' },
  ],
  tenantName: [{ required: true, message: '请输入租户名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择租户状态', trigger: 'change' }],
  timezone: [{ required: true, message: '请输入时区', trigger: 'blur' }],
  locale: [{ required: true, message: '请输入语言', trigger: 'blur' }],
}

function createEmptyDomainPayload(): TenantDomainPayload {
  return {
    domain: '',
    domainType: 'website',
    enabled: true,
  }
}

function resetForm() {
  Object.assign(form, createEmptyTenantPayload())
}

function addDomainRow() {
  form.domains.push(createEmptyDomainPayload())
}

function removeDomainRow(index: number) {
  form.domains.splice(index, 1)
}

function formatStatus(status: string) {
  if (status === 'ACTIVE') {
    return '启用'
  }
  if (status === 'DISABLED') {
    return '停用'
  }
  return status
}

function statusTagType(status: string) {
  if (status === 'ACTIVE') {
    return 'success'
  }
  if (status === 'DISABLED') {
    return 'danger'
  }
  return 'info'
}

function enabledDomainCount(tenant: TenantSummary) {
  return tenant.domains.filter((item) => item.enabled).length
}

async function loadData() {
  await runWithLoading(loading, async () => {
    const [current, list] = await Promise.all([fetchCurrentTenant(), fetchTenants()])
    currentTenant.value = current
    tenants.value = list
  }, '加载租户数据失败')
}

function openCreateDialog() {
  editingTenantId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(tenant: TenantSummary) {
  editingTenantId.value = tenant.id
  Object.assign(form, {
    tenantCode: tenant.tenantCode,
    tenantName: tenant.tenantName,
    status: tenant.status,
    timezone: tenant.timezone,
    locale: tenant.locale,
    remark: tenant.remark,
    domains: tenant.domains.map((domain) => ({
      domain: domain.domain,
      domainType: domain.domainType,
      enabled: domain.enabled,
    })),
  })
  dialogVisible.value = true
}

function onDialogClosed() {
  formRef.value?.clearValidate()
  editingTenantId.value = null
  resetForm()
}

async function onSave() {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  await runWithLoading(saving, async () => {
    const payload: TenantSavePayload = {
      tenantCode: form.tenantCode.trim(),
      tenantName: form.tenantName.trim(),
      status: form.status.trim(),
      timezone: form.timezone.trim(),
      locale: form.locale.trim(),
      remark: form.remark.trim(),
      domains: form.domains
        .map((item) => ({
          domain: item.domain.trim(),
          domainType: item.domainType.trim(),
          enabled: item.enabled,
        }))
        .filter((item) => item.domain),
    }

    if (editingTenantId.value) {
      await updateTenant(editingTenantId.value, payload)
      showSuccessMessage('租户已更新')
    } else {
      await createTenant(payload)
      showSuccessMessage('租户已创建')
    }

    dialogVisible.value = false
    await loadData()
  }, '保存租户失败')
}

onMounted(() => {
  void runSafely(loadData, '加载租户数据失败')
})
</script>
