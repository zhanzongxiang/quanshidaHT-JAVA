<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">工作台</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          展示当前后台运营概览与核心业务数据。
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
          <p v-if="item.tip" class="m-0 text-xs text-mist">{{ item.tip }}</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchDashboardSummary, type DashboardSummary } from '../api/dashboard'

const loading = ref(false)
const summary = ref<DashboardSummary | null>(null)

const metrics = computed(() => {
  const data = summary.value
  return [
    {
      label: '运单总数',
      value: data?.waybillTotal ?? '--',
      tip: data ? `在途 ${data.waybillInTransit} 单` : '',
    },
    {
      label: '新闻总数',
      value: data?.newsTotal ?? '--',
      tip: data ? `已发布 ${data.newsPublished} 篇` : '',
    },
    {
      label: '线路页面数',
      value: data?.serviceLineTotal ?? '--',
      tip: data?.homeContentUpdatedAt ? `首页最近更新：${data.homeContentUpdatedAt}` : '',
    },
  ]
})

async function loadSummary() {
  loading.value = true
  try {
    summary.value = await fetchDashboardSummary()
  } catch (error) {
    ElMessage.error('加载工作台数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadSummary()
})
</script>
