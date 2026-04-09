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
          <h1 class="m-0 text-2xl font-extrabold text-ink">后台管理</h1>
          <p class="m-0 text-sm text-mist">官网内容、线索与运单的一期运营后台。</p>
        </div>
        <div class="flex items-center gap-3 self-start sm:self-auto">
          <span class="rounded-full bg-slate-100 px-3 py-1 text-sm font-medium text-slate-700">
            {{ auth.me?.username }}
          </span>
          <el-button type="primary" @click="onLogout">退出登录</el-button>
        </div>
      </header>

      <section class="min-h-0 overflow-y-auto px-5 py-5 sm:px-6">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminMenuTreeItem from '../components/AdminMenuTreeItem.vue'
import { resetMenuRoutes } from '../router'
import { useAuthStore } from '../stores/auth'
import type { AdminMenu } from '../types/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const menus = computed(() => auth.me?.menus ?? [])
const activeMenu = computed(() => route.path)
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

function onLogout() {
  auth.logout()
  resetMenuRoutes()
  router.push('/login')
}
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
