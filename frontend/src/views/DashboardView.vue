<template>
  <div class="grid gap-4 md:grid-cols-3">
    <el-card
      v-for="item in metrics"
      :key="item.label"
      class="rounded-3xl border-0 shadow-panel"
    >
      <div class="space-y-2">
        <p class="m-0 text-sm font-medium text-mist">{{ item.label }}</p>
        <p class="m-0 text-4xl font-black tracking-tight text-slate-900">{{ item.value }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
const metrics = [
  { label: '今日新增线索', value: 18 },
  { label: '在途运单', value: 246 },
  { label: '待跟进客户', value: 31 },
]
</script>
