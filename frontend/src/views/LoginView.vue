<template>
  <div class="full-page-center">
    <el-card class="login-card">
      <template #header>
        <div class="title-wrap">
          <h2>QSD 管理后台</h2>
          <p>请输入账号密码进入系统</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="admin123" />
        </el-form-item>
        <el-button
          type="primary"
          class="submit"
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
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await auth.loginByPassword(form.username, form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.push(redirect)
  } catch (error) {
    ElMessage.error('登录失败，请检查账号密码')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-card {
  width: min(420px, 90vw);
  border-radius: 16px;
}

.title-wrap h2 {
  margin: 0;
}

.title-wrap p {
  margin: 6px 0 0;
  color: var(--subtext);
}

.submit {
  width: 100%;
  margin-top: 10px;
}
</style>
