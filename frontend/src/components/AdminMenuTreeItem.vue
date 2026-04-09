<template>
  <el-sub-menu v-if="hasChildren" :index="submenuIndex">
    <template #title>
      <span>{{ menu.name }}</span>
    </template>
    <AdminMenuTreeItem v-for="child in menu.children" :key="child.id" :menu="child" />
  </el-sub-menu>
  <el-menu-item v-else :index="menu.path" class="rounded-xl">
    <span>{{ menu.name }}</span>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AdminMenu } from '../types/auth'

const props = defineProps<{
  menu: AdminMenu
}>()

const hasChildren = computed(() => props.menu.children.length > 0)
const submenuIndex = computed(() => props.menu.path || `group-${props.menu.id}`)
</script>
