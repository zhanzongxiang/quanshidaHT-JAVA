<template>
  <div class="h-screen overflow-hidden bg-transparent lg:grid lg:grid-cols-[248px_minmax(0,1fr)]">
    <aside class="hidden h-screen overflow-y-auto border-r border-line bg-slate-950/95 px-3 py-5 text-slate-200 lg:block">
      <div class="mb-4 px-3 text-xl font-extrabold tracking-[0.08em] text-white">QSD Admin</div>
      <el-menu
        :default-active="activeMenu"
        :default-openeds="openMenuKeys"
        router
        background-color="transparent"
        text-color="#cbd5e1"
        active-text-color="#ffffff"
        class="admin-menu border-r-0"
      >
        <AdminMenuTreeItem v-for="menu in menus" :key="menu.id" :menu="menu" />
      </el-menu>
    </aside>

    <main class="grid h-screen min-h-0 grid-rows-[auto_minmax(0,1fr)] overflow-hidden">
      <header class="flex flex-col gap-4 border-b border-line bg-panel/90 px-5 py-5 backdrop-blur sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <div class="space-y-1">
          <h1 class="m-0 text-2xl font-extrabold text-ink">Admin Console</h1>
          <p class="m-0 text-sm text-mist">Unified operations console for content, waybills, members, and payments.</p>
        </div>
        <div class="flex flex-col items-stretch gap-3 self-start sm:items-end sm:self-auto">
          <div v-if="canSwitchTenant" class="flex flex-col gap-2 sm:min-w-[320px]">
            <div class="text-xs font-semibold uppercase tracking-[0.16em] text-mist">Current Tenant</div>
            <el-select
              :model-value="auth.me?.tenantId"
              :loading="tenantLoading || switchingTenant"
              filterable
              placeholder="Select tenant"
              @change="onTenantChange"
            >
              <el-option
                v-for="tenant in tenantOptions"
                :key="tenant.id"
                :label="buildTenantOptionLabel(tenant)"
                :value="tenant.id"
                :disabled="tenant.status !== 'ACTIVE'"
              />
            </el-select>
            <div class="text-xs text-mist">
              <template v-if="auth.me?.tenantSwitched">
                Acting on tenant `{{ auth.me?.tenantCode }}` with platform admin access. Login tenant: `{{ auth.me?.loginTenantCode }}`.
              </template>
              <template v-else>
                Current tenant: `{{ auth.me?.tenantCode }}`.
              </template>
            </div>
          </div>

          <div class="flex items-center justify-end gap-3">
            <span class="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-700">
              {{ auth.me?.username }}
            </span>
            <el-button type="primary" @click="onLogout">Sign out</el-button>
          </div>
        </div>
      </header>

      <section class="min-h-0 overflow-y-auto px-5 py-5 sm:px-6">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminMenuTreeItem from '../components/AdminMenuTreeItem.vue'
import { fetchTenants } from '../api/tenant'
import { DEFAULT_HOME_PATH, resetMenuRoutes } from '../router'
import { useAuthStore } from '../stores/auth'
import type { AdminMenu } from '../types/auth'
import type { TenantSummary } from '../types/tenant'
import { runSafely } from '../utils/async'
import { showSuccessMessage } from '../utils/message'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const tenantLoading = ref(false)
const switchingTenant = ref(false)
const tenantOptions = ref<TenantSummary[]>([])

const menus = computed(() => auth.me?.menus ?? [])
const activeMenu = computed(() => route.path)
const canSwitchTenant = computed(() => auth.hasPermission('tenant:edit'))
const openMenuKeys = computed(() => findParentPaths(menus.value, route.path))

function findParentPaths(menuTree: AdminMenu[], currentPath: string, parents: string[] = []): string[] {
  for (const menu of menuTree) {
    if (menu.path === currentPath) {
      return parents
    }

    if (menu.children.length > 0) {
      const key = menu.path || `group-${menu.id}`
      const result = findParentPaths(menu.children, currentPath, [...parents, key])
      if (result.length > 0) {
        return result
      }
    }
  }

  return []
}

function buildTenantOptionLabel(tenant: TenantSummary) {
  return `${tenant.tenantName} (${tenant.tenantCode})`
}

async function loadTenantOptions() {
  if (!canSwitchTenant.value) {
    tenantOptions.value = []
    return
  }

  tenantLoading.value = true
  try {
    tenantOptions.value = await fetchTenants()
  } finally {
    tenantLoading.value = false
  }
}

async function onTenantChange(tenantId: number) {
  if (!auth.me || auth.me.tenantId === tenantId) {
    return
  }

  switchingTenant.value = true
  try {
    resetMenuRoutes()
    await auth.switchTenant(tenantId)
    await loadTenantOptions()
    showSuccessMessage(`Switched to tenant ${auth.me?.tenantName ?? ''}`.trim())
    await router.replace(DEFAULT_HOME_PATH)
  } finally {
    switchingTenant.value = false
  }
}

function onLogout() {
  auth.logout()
  resetMenuRoutes()
  router.push('/login')
}

onMounted(() => {
  void runSafely(loadTenantOptions, 'Failed to load tenant list')
})
</script>

<style scoped>
.admin-menu :deep(.el-menu) {
  border-right: 0;
}

.admin-menu :deep(.el-menu-item) {
  margin: 4px 0;
  font-weight: 600;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(11, 95, 255, 0.92), rgba(0, 160, 111, 0.78));
}

.admin-menu :deep(.el-menu-item:hover) {
  background: rgba(148, 163, 184, 0.12);
}
</style>
