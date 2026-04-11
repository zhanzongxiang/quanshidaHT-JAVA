<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">线路页面</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          线路页面已经切换成固定模板编辑模式。每条线路都维护同一套模板模块，只改内容，不改版式。
        </p>
      </div>
      <div class="flex gap-3">
        <el-button :loading="loading" @click="loadServiceLines">刷新</el-button>
      </div>
    </div>

    <div class="grid gap-6 xl:grid-cols-3">
      <el-card v-for="line in serviceLines" :key="line.lineCode" class="rounded-3xl border-0 shadow-panel">
        <div class="space-y-4">
          <div class="flex items-start justify-between gap-3">
            <div>
              <h3 class="m-0 text-lg font-bold text-ink">{{ line.lineName }}</h3>
              <p class="m-0 mt-1 text-sm text-mist">前台路由：{{ line.linePath }}</p>
            </div>
            <el-tag :type="line.status === 'published' ? 'success' : 'warning'" effect="light">
              {{ line.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </div>

          <p class="m-0 text-sm leading-6 text-slate-600">{{ line.description }}</p>

          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="m-0 text-sm font-semibold text-slate-900">模板模块</p>
            <div class="mt-3 flex flex-wrap gap-2">
              <span
                v-for="section in sections"
                :key="section"
                class="rounded-full bg-white px-3 py-1 text-xs font-medium text-slate-600"
              >
                {{ section }}
              </span>
            </div>
          </div>

          <div class="rounded-2xl border border-dashed border-slate-300 p-4 text-sm text-mist">
            最近更新：{{ line.updatedAt || '未更新' }}<br />
            发布时间：{{ line.publishedAt || '未发布' }}
          </div>

          <el-button type="primary" class="w-full" @click="openEditor(line.lineCode)">编辑模板内容</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchServiceLineSummaries } from '../api/service-line'
import type { ServiceLineCode, ServiceLineSummary } from '../types/service-line'

const router = useRouter()

const loading = ref(false)
const serviceLines = ref<ServiceLineSummary[]>([])

const sections = ['基础信息', 'Hero 首屏', '我们的优势', '服务流程', '承运范围', '底部 CTA', 'SEO']

async function loadServiceLines() {
  loading.value = true
  try {
    serviceLines.value = await fetchServiceLineSummaries()
  } catch {
    ElMessage.error('加载线路页面失败')
  } finally {
    loading.value = false
  }
}

function openEditor(lineCode: ServiceLineCode) {
  router.push(`/pages/service-lines/${lineCode}`)
}

onMounted(() => {
  loadServiceLines()
})
</script>
