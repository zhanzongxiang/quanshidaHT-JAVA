<template>
  <div class="layout-shell">
    <aside class="sider">
      <div class="brand">QSD Admin</div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        router
      >
        <el-menu-item
          v-for="menu in menus"
          :key="menu.id"
          :index="menu.path"
        >
          <span>{{ menu.name }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <main class="main">
      <header class="header">
        <div>
          <h1>后台管理</h1>
          <p>官网内容、线索与运单的一期运营后台</p>
        </div>
        <div class="right-tools">
          <span>{{ auth.me?.username }}</span>
          <el-button type="primary" @click="onLogout">退出登录</el-button>
        </div>
      </header>
      <section class="content-area">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const menus = computed(() => auth.me?.menus ?? [])
const activeMenu = computed(() => route.path)

function onLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 248px 1fr;
}

.sider {
  border-right: 1px solid var(--border);
  background: linear-gradient(180deg, #0f172a 0%, #1f2937 100%);
  color: #e5e7eb;
  padding: 18px 12px;
}

.brand {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.2px;
  margin: 4px 10px 16px;
}

.menu {
  border-right: 0;
  background: transparent;
}

.main {
  display: grid;
  grid-template-rows: auto 1fr;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border);
  padding: 18px 24px;
  background: var(--panel);
}

.header h1 {
  margin: 0;
  font-size: 22px;
}

.header p {
  margin: 6px 0 0;
  color: var(--subtext);
}

.right-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content-area {
  padding: 20px 24px;
}

@media (max-width: 900px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .sider {
    display: none;
  }
}
</style>
