<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 class="m-0 text-xl font-extrabold text-ink">页脚设置</h2>
          <p class="m-0 mt-2 max-w-3xl text-sm leading-6 text-mist">维护公司说明、快捷链接、关注我们和版权信息。当前版本先做本地保存。</p>
        </div>
        <el-button type="primary" :loading="saving" @click="onSave">保存设置</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <el-form label-position="top" class="grid gap-4 md:grid-cols-2">
        <el-form-item label="公司标题">
          <el-input v-model="form.companyTitle" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="公司简介" class="md:col-span-2">
          <el-input v-model="form.companyDescription" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="关注我们">
          <el-input v-model="form.followUs" />
        </el-form-item>
        <el-form-item label="快捷链接（每行一个）" class="md:col-span-2">
          <el-input v-model="form.quickLinks" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="版权信息" class="md:col-span-2">
          <el-input v-model="form.copyright" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { fetchFooterSettings, saveFooterSettings } from '../api/site-settings'
import type { FooterSettings } from '../types/site-settings'

const saving = ref(false)
const form = reactive<FooterSettings>({
  companyTitle: '',
  companyDescription: '',
  phone: '',
  email: '',
  quickLinks: '',
  followUs: '',
  copyright: '',
})

async function loadSettings() {
  Object.assign(form, await fetchFooterSettings())
}

async function onSave() {
  saving.value = true
  try {
    await saveFooterSettings({ ...form })
    ElMessage.success('页脚设置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>
