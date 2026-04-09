<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 class="m-0 text-xl font-extrabold text-ink">导航设置</h2>
          <p class="m-0 mt-2 max-w-3xl text-sm leading-6 text-mist">维护前台顶部导航品牌信息和菜单项。当前版本先做本地保存。</p>
        </div>
        <el-button type="primary" :loading="saving" @click="onSave">保存设置</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="grid gap-4 md:grid-cols-2">
        <el-form label-position="top">
          <el-form-item label="品牌名称">
            <el-input v-model="form.brandName" />
          </el-form-item>
          <el-form-item label="Logo 链接">
            <el-input v-model="form.logoUrl" />
          </el-form-item>
        </el-form>
      </div>
      <div class="mt-4 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <div v-for="item in form.items" :key="item.id" class="rounded-2xl border border-slate-200 bg-white p-4">
          <div class="mb-3 flex items-center justify-between gap-3">
            <p class="m-0 text-sm font-semibold text-slate-900">{{ item.label }}</p>
            <el-switch v-model="item.enabled" />
          </div>
          <el-form label-position="top">
            <el-form-item label="菜单名称">
              <el-input v-model="item.label" />
            </el-form-item>
            <el-form-item label="链接地址">
              <el-input v-model="item.path" />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { fetchNavigationSettings, saveNavigationSettings } from '../api/site-settings'
import type { NavigationSettings } from '../types/site-settings'

const saving = ref(false)
const form = reactive<NavigationSettings>({
  brandName: '',
  logoUrl: '',
  items: [],
})

async function loadSettings() {
  const data = await fetchNavigationSettings()
  form.brandName = data.brandName
  form.logoUrl = data.logoUrl
  form.items.splice(0, form.items.length, ...data.items.map((item) => ({ ...item })))
}

async function onSave() {
  saving.value = true
  try {
    await saveNavigationSettings({
      brandName: form.brandName,
      logoUrl: form.logoUrl,
      items: form.items.map((item) => ({ ...item })),
    })
    ElMessage.success('导航设置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>
