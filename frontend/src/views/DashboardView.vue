<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-[28px] border border-line bg-[linear-gradient(135deg,rgba(8,47,73,0.96),rgba(15,118,110,0.9))] px-6 py-6 text-white shadow-panel">
      <div class="flex flex-col gap-5 xl:flex-row xl:items-end xl:justify-between">
        <div class="space-y-3">
          <div class="inline-flex items-center rounded-full bg-white/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-white/80">
            Tenant Operations
          </div>
          <div>
            <h2 class="m-0 text-3xl font-black tracking-tight">{{ summary?.tenantName ?? auth.me?.tenantName ?? 'Current Tenant' }}</h2>
            <p class="m-0 mt-2 max-w-3xl text-sm leading-6 text-white/80">
              Operational overview for the current tenant, covering members, waybills, content, payments, and tenant setup readiness.
            </p>
          </div>
        </div>

        <div class="grid gap-3 sm:grid-cols-2 xl:min-w-[360px]">
          <div class="rounded-2xl border border-white/15 bg-white/10 px-4 py-3 backdrop-blur">
            <div class="text-xs uppercase tracking-[0.14em] text-white/60">Tenant Code</div>
            <div class="mt-2 text-lg font-bold">{{ summary?.tenantCode ?? '--' }}</div>
          </div>
          <div class="rounded-2xl border border-white/15 bg-white/10 px-4 py-3 backdrop-blur">
            <div class="text-xs uppercase tracking-[0.14em] text-white/60">Mode</div>
            <div class="mt-2 text-lg font-bold">{{ auth.me?.tenantSwitched ? 'Switched' : 'Direct' }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4" v-loading="loading">
      <el-card v-for="item in headlineMetrics" :key="item.label" class="rounded-3xl border-0 shadow-panel">
        <div class="space-y-2">
          <div class="text-xs font-semibold uppercase tracking-[0.14em] text-mist">{{ item.label }}</div>
          <div class="text-4xl font-black tracking-tight text-slate-900">{{ item.value }}</div>
          <div class="text-sm text-slate-600">{{ item.tip }}</div>
        </div>
      </el-card>
    </div>

    <div class="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
      <el-card class="rounded-3xl border-0 shadow-panel" v-loading="loading">
        <template #header>
          <div>
            <h3 class="m-0 text-base font-bold text-ink">Operations Summary</h3>
            <p class="m-0 mt-1 text-sm text-mist">Core tenant activity and readiness indicators.</p>
          </div>
        </template>

        <div class="grid gap-4 md:grid-cols-2">
          <div v-for="item in operationMetrics" :key="item.label" class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4">
            <div class="text-xs uppercase tracking-[0.12em] text-mist">{{ item.label }}</div>
            <div class="mt-2 text-2xl font-black text-slate-900">{{ item.value }}</div>
            <div class="mt-1 text-sm text-slate-600">{{ item.tip }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="rounded-3xl border-0 shadow-panel" v-loading="loading">
        <template #header>
          <div>
            <h3 class="m-0 text-base font-bold text-ink">Tenant Setup</h3>
            <p class="m-0 mt-1 text-sm text-mist">Base tenant configuration and content bootstrap status.</p>
          </div>
        </template>

        <div class="space-y-3">
          <div v-for="item in setupItems" :key="item.label" class="flex items-start justify-between gap-4 rounded-2xl border border-slate-200 px-4 py-3">
            <div>
              <div class="text-sm font-semibold text-slate-900">{{ item.label }}</div>
              <div class="mt-1 text-sm text-slate-600">{{ item.tip }}</div>
            </div>
            <el-tag :type="item.type" effect="plain">{{ item.value }}</el-tag>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { fetchDashboardSummary, type DashboardSummary } from '../api/dashboard'
import { useAuthStore } from '../stores/auth'
import { showErrorMessage } from '../utils/message'

const auth = useAuthStore()
const loading = ref(false)
const summary = ref<DashboardSummary | null>(null)

const headlineMetrics = computed(() => {
  const data = summary.value
  return [
    {
      label: 'Members',
      value: data?.memberTotal ?? '--',
      tip: data ? `${data.memberEnabled} enabled, ${data.memberWechatBound} bound to WeChat` : 'Loading member status',
    },
    {
      label: 'Waybills',
      value: data?.waybillTotal ?? '--',
      tip: data ? `${data.waybillInTransit} currently in transit` : 'Loading waybill status',
    },
    {
      label: 'Payments',
      value: data?.payOrderTotal ?? '--',
      tip: data ? `${data.payOrderPaid} paid, ${data.payOrderPaying} awaiting completion` : 'Loading payment status',
    },
    {
      label: 'Content',
      value: data?.newsTotal ?? '--',
      tip: data ? `${data.newsPublished} published articles, ${data.serviceLineTotal} service pages` : 'Loading content status',
    },
  ]
})

const operationMetrics = computed(() => {
  const data = summary.value
  return [
    {
      label: 'Enabled Domains',
      value: data?.enabledDomainCount ?? '--',
      tip: 'Public-site routing domains currently active',
    },
    {
      label: 'Enabled Merchants',
      value: data?.enabledMerchantCount ?? '--',
      tip: 'Payment merchant profiles available to this tenant',
    },
    {
      label: 'Refund Orders',
      value: data?.refundOrderTotal ?? '--',
      tip: data ? `${data.refundProcessing} refunds still processing` : 'Loading refund status',
    },
    {
      label: 'Tenant Locale',
      value: data ? `${data.locale}` : '--',
      tip: data ? `Timezone: ${data.timezone}` : 'Loading locale settings',
    },
  ]
})

const setupItems = computed(() => {
  const data = summary.value
  return [
    {
      label: 'Tenant Status',
      value: formatTenantStatus(data?.tenantStatus),
      tip: 'Whether the tenant is available for routing and operations',
      type: data?.tenantStatus === 'ACTIVE' ? 'success' : 'danger',
    },
    {
      label: 'Home Content',
      value: data?.homeContentStatus ? formatContentStatus(data.homeContentStatus) : 'Missing',
      tip: data?.homeContentUpdatedAt ? `Last updated at ${data.homeContentUpdatedAt}` : 'No home content update detected yet',
      type: data?.homeContentStatus === 'published' ? 'success' : 'warning',
    },
    {
      label: 'Service Line Pages',
      value: data?.serviceLineTotal != null ? `${data.serviceLineTotal}` : '--',
      tip: 'Tenant bootstrap service-line pages currently stored',
      type: data && data.serviceLineTotal > 0 ? 'success' : 'warning',
    },
    {
      label: 'Operating Scope',
      value: auth.me?.tenantSwitched ? 'Cross-tenant' : 'Local tenant',
      tip: auth.me?.tenantSwitched
        ? `Login tenant: ${auth.me?.loginTenantCode}`
        : 'You are operating inside the login tenant context',
      type: auth.me?.tenantSwitched ? 'warning' : 'info',
    },
  ]
})

function formatTenantStatus(status?: string | null) {
  if (status === 'ACTIVE') {
    return 'Active'
  }
  if (status === 'DISABLED') {
    return 'Disabled'
  }
  return status || '--'
}

function formatContentStatus(status?: string | null) {
  if (!status) {
    return '--'
  }
  return status.charAt(0).toUpperCase() + status.slice(1)
}

async function loadSummary() {
  loading.value = true
  try {
    summary.value = await fetchDashboardSummary()
  } catch (error) {
    showErrorMessage(error, 'Failed to load dashboard summary')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void loadSummary()
})
</script>
