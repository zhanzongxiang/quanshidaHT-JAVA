<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>内容管理</span>
        <el-button v-if="canEdit" type="primary">新增内容块</el-button>
      </div>
    </template>

    <el-table :data="rows">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="page" label="页面" />
      <el-table-column prop="updatedAt" label="更新时间" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const canEdit = computed(() => auth.hasPermission('content:edit'))

const rows = [
  { title: '首页 Banner', page: 'home', updatedAt: '2026-03-31 15:00' },
  { title: '联系我们', page: 'contact', updatedAt: '2026-03-31 14:22' },
]
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
