<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 class="m-0 text-xl font-extrabold text-ink">联系我们</h2>
          <p class="m-0 mt-2 max-w-3xl text-sm leading-6 text-mist">
            维护官网“联系我们”模块的首屏文案、联系卡片、办公时间和转化按钮。当前仍然先保存在本地设置中。
          </p>
        </div>
        <el-button type="primary" :loading="saving" @click="onSave">保存设置</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <template #header>
        <div>
          <h3 class="m-0 text-base font-bold text-ink">首屏文案</h3>
          <p class="m-0 mt-1 text-sm text-mist">用于联系页顶部横幅和页面开场说明。</p>
        </div>
      </template>

      <el-form label-position="top" class="grid gap-4 md:grid-cols-2">
        <el-form-item label="角标文案">
          <el-input v-model="form.heroBadge" maxlength="30" show-word-limit />
        </el-form-item>
        <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-mist">
          建议把“响应速度”或“服务时段”放在角标里，避免堆砌企业介绍。
        </div>
        <el-form-item label="主标题" class="md:col-span-2">
          <el-input v-model="form.heroTitle" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="副标题" class="md:col-span-2">
          <el-input v-model="form.heroSubtitle" type="textarea" :rows="4" maxlength="220" show-word-limit />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <template #header>
        <div>
          <h3 class="m-0 text-base font-bold text-ink">联系卡片</h3>
          <p class="m-0 mt-1 text-sm text-mist">四张固定卡片分别对应热线、邮箱、微信和办公地址。</p>
        </div>
      </template>

      <div class="grid gap-4 xl:grid-cols-2">
        <div class="rounded-2xl border border-slate-200 p-4">
          <p class="m-0 text-sm font-semibold text-slate-900">业务热线</p>
          <el-form label-position="top" class="mt-3">
            <el-form-item label="标题">
              <el-input v-model="form.hotlineTitle" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="号码">
              <el-input v-model="form.hotlineNumber" maxlength="40" show-word-limit />
            </el-form-item>
            <el-form-item label="说明">
              <el-input v-model="form.hotlineDescription" type="textarea" :rows="3" maxlength="140" show-word-limit />
            </el-form-item>
          </el-form>
        </div>

        <div class="rounded-2xl border border-slate-200 p-4">
          <p class="m-0 text-sm font-semibold text-slate-900">商务邮箱</p>
          <el-form label-position="top" class="mt-3">
            <el-form-item label="标题">
              <el-input v-model="form.emailTitle" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="form.emailAddress" maxlength="60" show-word-limit />
            </el-form-item>
            <el-form-item label="说明">
              <el-input v-model="form.emailDescription" type="textarea" :rows="3" maxlength="140" show-word-limit />
            </el-form-item>
          </el-form>
        </div>

        <div class="rounded-2xl border border-slate-200 p-4">
          <p class="m-0 text-sm font-semibold text-slate-900">客户顾问微信</p>
          <el-form label-position="top" class="mt-3">
            <el-form-item label="标题">
              <el-input v-model="form.wechatTitle" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="微信号">
              <el-input v-model="form.wechatId" maxlength="40" show-word-limit />
            </el-form-item>
            <el-form-item label="说明">
              <el-input v-model="form.wechatDescription" type="textarea" :rows="3" maxlength="140" show-word-limit />
            </el-form-item>
          </el-form>
        </div>

        <div class="rounded-2xl border border-slate-200 p-4">
          <p class="m-0 text-sm font-semibold text-slate-900">办公地址</p>
          <el-form label-position="top" class="mt-3">
            <el-form-item label="标题">
              <el-input v-model="form.addressTitle" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="地址">
              <el-input v-model="form.addressLine" type="textarea" :rows="2" maxlength="120" show-word-limit />
            </el-form-item>
            <el-form-item label="说明">
              <el-input v-model="form.addressDescription" type="textarea" :rows="3" maxlength="140" show-word-limit />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <div class="grid gap-6 xl:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
      <el-card class="rounded-3xl border-0 shadow-panel">
        <template #header>
          <div>
            <h3 class="m-0 text-base font-bold text-ink">办公时间与服务承诺</h3>
            <p class="m-0 mt-1 text-sm text-mist">用于联系页的辅助信息区，帮助用户快速判断是否要立即咨询。</p>
          </div>
        </template>

        <el-form label-position="top" class="grid gap-4 md:grid-cols-2">
          <el-form-item label="模块标题" class="md:col-span-2">
            <el-input v-model="form.officeHoursTitle" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="工作日时间">
            <el-input v-model="form.weekdayHours" maxlength="60" show-word-limit />
          </el-form-item>
          <el-form-item label="周末时间">
            <el-input v-model="form.weekendHours" maxlength="60" show-word-limit />
          </el-form-item>
          <el-form-item label="响应承诺" class="md:col-span-2">
            <el-input v-model="form.responseCommitment" type="textarea" :rows="3" maxlength="160" show-word-limit />
          </el-form-item>
          <el-form-item label="到访提示" class="md:col-span-2">
            <el-input v-model="form.visitNotice" type="textarea" :rows="3" maxlength="180" show-word-limit />
          </el-form-item>
          <el-form-item label="地图说明" class="md:col-span-2">
            <el-input v-model="form.mapHint" type="textarea" :rows="3" maxlength="180" show-word-limit />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="rounded-3xl border-0 shadow-panel">
        <template #header>
          <div>
            <h3 class="m-0 text-base font-bold text-ink">服务承诺列表</h3>
            <p class="m-0 mt-1 text-sm text-mist">一行一条，用于联系页右侧或底部的信任补充区。</p>
          </div>
        </template>

        <el-form label-position="top">
          <el-form-item label="承诺内容">
            <el-input v-model="form.servicePromises" type="textarea" :rows="10" />
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <template #header>
        <div>
          <h3 class="m-0 text-base font-bold text-ink">行动引导</h3>
          <p class="m-0 mt-1 text-sm text-mist">用于联系页底部 CTA 或首屏按钮组。</p>
        </div>
      </template>

      <el-form label-position="top" class="grid gap-4 md:grid-cols-2">
        <el-form-item label="CTA 标题" class="md:col-span-2">
          <el-input v-model="form.ctaTitle" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="CTA 说明" class="md:col-span-2">
          <el-input v-model="form.ctaSubtitle" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="主按钮文案">
          <el-input v-model="form.primaryButtonText" maxlength="24" show-word-limit />
        </el-form-item>
        <el-form-item label="主按钮链接">
          <el-input v-model="form.primaryButtonLink" placeholder="/contact/form" />
        </el-form-item>
        <el-form-item label="次按钮文案">
          <el-input v-model="form.secondaryButtonText" maxlength="24" show-word-limit />
        </el-form-item>
        <el-form-item label="次按钮链接">
          <el-input v-model="form.secondaryButtonLink" placeholder="/services" />
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
  heroBadge: '',
  heroTitle: '',
  heroSubtitle: '',
  hotlineTitle: '',
  hotlineNumber: '',
  hotlineDescription: '',
  emailTitle: '',
  emailAddress: '',
  emailDescription: '',
  wechatTitle: '',
  wechatId: '',
  wechatDescription: '',
  addressTitle: '',
  addressLine: '',
  addressDescription: '',
  officeHoursTitle: '',
  weekdayHours: '',
  weekendHours: '',
  responseCommitment: '',
  visitNotice: '',
  ctaTitle: '',
  ctaSubtitle: '',
  primaryButtonText: '',
  primaryButtonLink: '',
  secondaryButtonText: '',
  secondaryButtonLink: '',
  mapHint: '',
  servicePromises: '',
})

async function loadSettings() {
  Object.assign(form, await fetchContactSettings())
}

async function onSave() {
  saving.value = true
  try {
    await saveContactSettings({ ...form })
    ElMessage.success('联系我们模块已保存')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadSettings()
})
</script>
