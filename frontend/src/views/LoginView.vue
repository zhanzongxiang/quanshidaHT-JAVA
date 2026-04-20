<template>
  <div class="flex min-h-screen items-center justify-center px-4 py-10">
    <el-card class="w-full max-w-[420px] rounded-3xl border-0 shadow-panel">
      <template #header>
        <div class="space-y-1">
          <p class="text-xs font-semibold uppercase tracking-[0.3em] text-brand">QSD Admin</p>
          <h2 class="m-0 text-3xl font-extrabold text-ink">登录后台</h2>
          <p class="m-0 text-sm text-mist">请输入账号密码进入系统。</p>
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="admin123" />
        </el-form-item>
        <el-button
          type="primary"
          class="mt-3 !flex !w-full !justify-center"
          :loading="submitting"
          @click="onSubmit"
        >
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { nextTick, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DEFAULT_HOME_PATH, ensureMenuRoutes } from '../router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  if (!formRef.value || submitting.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await auth.loginByPassword(form.username, form.password)

    if (!auth.me) {
      throw new Error('missing me after login')
    }

    ensureMenuRoutes(auth.me.menus)

    const redirect = typeof route.query.redirect === 'string'
      ? route.query.redirect
      : DEFAULT_HOME_PATH

    const target = router.resolve(redirect)

    await nextTick()
    await router.replace(target.fullPath)
    await router.isReady()
  } catch (error) {
    ElMessage.error('登录失败，请检查账号密码或接口状态。')
  } finally {
    submitting.value = false
  }
}
</script>
