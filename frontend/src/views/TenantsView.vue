<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">Tenant Management</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          Manage tenant settings, bound domains, and activation status for the shared multi-company platform.
        </p>
      </div>
      <div class="flex gap-3">
        <el-button :loading="loading" @click="loadData">Refresh</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">New Tenant</el-button>
      </div>
    </div>

    <div class="grid gap-4 xl:grid-cols-[360px_minmax(0,1fr)]">
      <el-card class="rounded-3xl border-0 shadow-panel">
        <template #header>
          <div>
            <h3 class="m-0 text-base font-bold text-ink">Current Tenant Context</h3>
            <p class="m-0 mt-1 text-sm text-mist">Tenant resolved from the current request and authenticated session.</p>
          </div>
        </template>

        <div v-if="currentTenant" class="space-y-4">
          <div class="rounded-2xl border border-slate-200 bg-white px-4 py-4">
            <div class="text-xs uppercase tracking-[0.2em] text-mist">Tenant Code</div>
            <div class="mt-2 text-lg font-bold text-ink">{{ currentTenant.tenantCode }}</div>
          </div>

          <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">Tenant Name</div>
              <div class="mt-1 font-semibold text-ink">{{ currentTenant.tenantName }}</div>
            </div>
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">Status</div>
              <div class="mt-1">
                <el-tag :type="statusTagType(currentTenant.status)">{{ formatStatus(currentTenant.status) }}</el-tag>
              </div>
            </div>
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">Timezone</div>
              <div class="mt-1 font-medium text-ink">{{ currentTenant.timezone }}</div>
            </div>
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">Locale</div>
              <div class="mt-1 font-medium text-ink">{{ currentTenant.locale }}</div>
            </div>
            <div class="rounded-2xl border border-slate-200 px-4 py-3">
              <div class="text-xs text-mist">Enabled Domains</div>
              <div class="mt-1 font-medium text-ink">{{ enabledDomainCount(currentTenant) }}</div>
            </div>
          </div>

          <div v-if="currentTenant.bootstrapAdmin" class="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-4">
            <div class="text-xs uppercase tracking-[0.2em] text-amber-700">Bootstrap Admin</div>
            <div class="mt-2 text-sm text-slate-700">
              Username:
              <span class="font-semibold text-slate-900">{{ currentTenant.bootstrapAdmin.username }}</span>
            </div>
            <div class="mt-1 text-sm text-slate-700">
              Initial Password:
              <span class="font-semibold text-slate-900">{{ currentTenant.bootstrapAdmin.initialPassword }}</span>
            </div>
          </div>

          <div class="rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-4 py-3 text-sm text-mist">
            {{ currentTenant.remark || 'No tenant remark.' }}
          </div>
        </div>
      </el-card>

      <el-card class="rounded-3xl border-0 shadow-panel">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">Tenant List</h3>
              <p class="m-0 mt-1 text-sm text-mist">Platform admins can maintain tenant configuration and access domains here.</p>
            </div>
            <el-tag type="info">{{ tenants.length }} tenants</el-tag>
          </div>
        </template>

        <el-table :data="tenants" v-loading="loading" border class="overflow-hidden rounded-2xl">
          <el-table-column prop="tenantCode" label="Tenant Code" min-width="140" />
          <el-table-column prop="tenantName" label="Tenant Name" min-width="180" />
          <el-table-column label="Status" width="120">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="timezone" label="Timezone" min-width="150" />
          <el-table-column prop="locale" label="Locale" min-width="120" />
          <el-table-column label="Domains" min-width="260">
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
                <span v-if="row.domains.length === 0" class="text-sm text-mist">Not configured</span>
                <span v-else-if="row.domains.length > 3" class="text-sm text-mist">+{{ row.domains.length - 3 }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="Bootstrap Admin" min-width="200">
            <template #default="{ row }">
              <div v-if="row.bootstrapAdmin" class="text-sm text-slate-700">
                <div>{{ row.bootstrapAdmin.username }}</div>
                <div class="text-mist">{{ row.bootstrapAdmin.initialPassword }}</div>
              </div>
              <span v-else class="text-sm text-mist">Not initialized</span>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="Updated At" min-width="180" />
          <el-table-column label="Actions" width="120" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openEditDialog(row)">{{ canEdit ? 'Edit' : 'View' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingTenantId ? 'Edit Tenant' : 'New Tenant'"
      width="920px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" :disabled="!canEdit" label-position="top" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">Base Settings</h3>
              <p class="m-0 mt-1 text-sm text-mist">
                Tenant code is used by platform routing and context resolution. Disabling a tenant is protected by platform rules.
              </p>
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="Tenant Code" prop="tenantCode">
              <el-input v-model="form.tenantCode" maxlength="64" placeholder="default / acme" />
            </el-form-item>
            <el-form-item label="Tenant Name" prop="tenantName">
              <el-input v-model="form.tenantName" maxlength="128" placeholder="Acme Logistics" />
            </el-form-item>
            <el-form-item label="Status" prop="status">
              <el-select v-model="form.status">
                <el-option label="Active" value="ACTIVE" />
                <el-option label="Disabled" value="DISABLED" />
              </el-select>
            </el-form-item>
            <el-form-item label="Timezone" prop="timezone">
              <el-input v-model="form.timezone" maxlength="64" placeholder="Asia/Shanghai" />
            </el-form-item>
            <el-form-item label="Locale" prop="locale">
              <el-input v-model="form.locale" maxlength="32" placeholder="zh-CN" />
            </el-form-item>
            <el-form-item label="Remark" prop="remark" class="md:col-span-2">
              <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-base font-bold text-ink">Domain Management</h3>
                <p class="m-0 mt-1 text-sm text-mist">
                  Bind multiple domains to a tenant. Enabled domains participate in public-site tenant resolution.
                </p>
              </div>
              <el-button v-if="canEdit" type="primary" plain @click="addDomainRow">Add Domain</el-button>
            </div>
          </template>

          <div v-if="form.domains.length > 0" class="space-y-4">
            <div
              v-for="(domain, index) in form.domains"
              :key="`domain-${index}`"
              class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4"
            >
              <div class="grid gap-4 md:grid-cols-[minmax(0,1.6fr)_180px_120px_auto]">
                <el-form-item :label="`Domain ${index + 1}`" :prop="`domains.${index}.domain`" class="mb-0">
                  <el-input v-model="domain.domain" maxlength="255" placeholder="acme.example.com" />
                </el-form-item>
                <el-form-item :label="`Type ${index + 1}`" :prop="`domains.${index}.domainType`" class="mb-0">
                  <el-select v-model="domain.domainType">
                    <el-option label="website" value="website" />
                    <el-option label="admin" value="admin" />
                    <el-option label="api" value="api" />
                  </el-select>
                </el-form-item>
                <el-form-item :label="`Enabled ${index + 1}`" class="mb-0">
                  <el-switch v-model="domain.enabled" />
                </el-form-item>
                <div class="flex items-end justify-end">
                  <el-button v-if="canEdit" type="danger" plain @click="removeDomainRow(index)">Remove</el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="No domains configured" />
        </el-card>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">{{ canEdit ? 'Cancel' : 'Close' }}</el-button>
          <el-button v-if="canEdit" type="primary" :loading="saving" @click="onSave">Save</el-button>
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
    { required: true, message: 'Enter tenant code', trigger: 'blur' },
    { min: 2, max: 64, message: 'Tenant code length must be between 2 and 64', trigger: 'blur' },
  ],
  tenantName: [{ required: true, message: 'Enter tenant name', trigger: 'blur' }],
  status: [{ required: true, message: 'Select tenant status', trigger: 'change' }],
  timezone: [{ required: true, message: 'Enter timezone', trigger: 'blur' }],
  locale: [{ required: true, message: 'Enter locale', trigger: 'blur' }],
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
    return 'Active'
  }
  if (status === 'DISABLED') {
    return 'Disabled'
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
  }, 'Failed to load tenant data')
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
      showSuccessMessage('Tenant updated')
    } else {
      await createTenant(payload)
      showSuccessMessage('Tenant created')
    }

    dialogVisible.value = false
    await loadData()
  }, 'Failed to save tenant')
}

onMounted(() => {
  void runSafely(loadData, 'Failed to load tenant data')
})
</script>
