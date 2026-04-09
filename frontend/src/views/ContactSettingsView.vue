<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 class="m-0 text-xl font-extrabold text-ink">联系方式</h2>
          <p class="m-0 mt-2 max-w-3xl text-sm leading-6 text-mist">维护全站联系电话、邮箱、地址和微信号。当前版本先做本地保存。</p>
        </div>
        <el-button type="primary" :loading="saving" @click="onSave">保存设置</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <el-form label-position="top" class="grid gap-4 md:grid-cols-2">
        <el-form-item label="模块标题" class="md:col-span-2">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="模块副标题" class="md:col-span-2">
          <el-input v-model="form.subtitle" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="联系地址" class="md:col-span-2">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="微信号">
          <el-input v-model="form.wechat" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { fetchContactSettings, saveContactSettings } from '../api/site-settings'
import type { ContactSettings } from '../types/site-settings'

const saving = ref(false)
const form = reactive<ContactSettings>({
  title: '',
  subtitle: '',
  phone: '',
  email: '',
  address: '',
  wechat: '',
})

async function loadSettings() {
  Object.assign(form, await fetchContactSettings())
}

async function onSave() {
  saving.value = true
  try {
    await saveContactSettings({ ...form })
    ElMessage.success('联系方式已保存')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>
