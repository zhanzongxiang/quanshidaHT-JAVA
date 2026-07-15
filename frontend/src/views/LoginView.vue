<template>
  <div class="flex min-h-screen items-center justify-center px-4 py-10">
    <el-card class="w-full max-w-[420px] rounded-3xl border-0 shadow-panel">
      <template #header>
        <div class="space-y-1">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-brand">QSD Admin</p>
          <h2 class="m-0 text-3xl font-extrabold text-ink">Admin Sign In</h2>
          <p class="m-0 text-sm text-mist">Enter your account credentials to access the operations console.</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="space-y-1"
        @submit.prevent="onSubmit"
      >
        <el-form-item label="Username" prop="username">
          <el-input v-model.trim="form.username" placeholder="Enter username" maxlength="64" />
        </el-form-item>
        <el-form-item label="Password" prop="password">
          <el-input v-model.trim="form.password" type="password" show-password placeholder="Enter password" maxlength="64" />
        </el-form-item>
        <el-button
          type="primary"
          class="mt-3 !flex !w-full !justify-center"
          :loading="submitting"
          @click="onSubmit"
        >
          Sign In
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { showErrorMessage } from '../utils/message'

const auth = useAuthStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: 'Enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Enter password', trigger: 'blur' }],
}

async function onSubmit() {
  if (!formRef.value || submitting.value) {
    return
  }

  const valid = await formRef.value.validate().then(() => true).catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    await auth.loginByPassword(form.username, form.password)
    if (!auth.me) {
      throw new Error('Sign-in succeeded but account details were not loaded')
    }
    window.location.href = '/admin/dashboard'
  } catch (error) {
    showErrorMessage(error, 'Sign-in failed, please verify credentials and service availability')
  } finally {
    submitting.value = false
  }
}
</script>
