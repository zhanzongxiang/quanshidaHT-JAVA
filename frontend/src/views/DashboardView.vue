<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">工作台</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          后台首页保持和其他页面一致的面板背景与留白，用于展示当前运营概览。
        </p>
      </div>
    </div>

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
  </div>
</template>

<script setup lang="ts">
const metrics = [
  { label: '今日新增线索', value: 18 },
  { label: '在途运单', value: 246 },
  { label: '待跟进客户', value: 31 },
]
</script>
