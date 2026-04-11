<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <div class="flex items-center gap-3">
          <el-button text @click="router.push('/pages/service-lines')">返回线路列表</el-button>
          <el-tag :type="document.status === 'published' ? 'success' : 'warning'" effect="light">
            {{ document.status === 'published' ? '已发布' : '草稿' }}
          </el-tag>
        </div>
        <h2 class="m-0 text-xl font-extrabold text-ink">{{ document.lineName || '线路模板编辑器' }}</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          当前编辑器已经按前台服务线路 schema 校准，维护的是统一模板字段，而不是自由拼装布局。
        </p>
        <p class="m-0 text-xs text-mist">
          最近更新：{{ document.updatedAt || '未更新' }}，发布时间：{{ document.publishedAt || '未发布' }}
        </p>
      </div>
      <div class="flex flex-wrap gap-3">
        <el-button :loading="loading" @click="loadDocument">刷新</el-button>
        <el-button type="primary" plain :loading="savingDraft" @click="onSaveDraft">保存草稿</el-button>
        <el-button type="primary" :loading="publishing" @click="onPublish">发布</el-button>
      </div>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-6" v-loading="loading">
      <el-card shadow="never" class="rounded-3xl border border-slate-200">
        <template #header>
          <div>
            <h3 class="m-0 text-base font-bold text-ink">Hero 基础信息</h3>
            <p class="m-0 mt-1 text-sm text-mist">对应前台线路页的 `eyebrow`、标题、副标题、说明和主视觉图。</p>
          </div>
        </template>

        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="线路标识" prop="key">
            <el-input v-model="form.key" disabled />
          </el-form-item>
          <el-form-item label="Eyebrow" prop="eyebrow">
            <el-input v-model="form.eyebrow" maxlength="40" show-word-limit />
          </el-form-item>
          <el-form-item label="标题" prop="title" class="md:col-span-2">
            <el-input v-model="form.title" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="副标题" prop="subtitle" class="md:col-span-2">
            <el-input v-model="form.subtitle" type="textarea" :rows="3" maxlength="220" show-word-limit />
          </el-form-item>
          <el-form-item label="描述文案" prop="description" class="md:col-span-2">
            <el-input v-model="form.description" type="textarea" :rows="4" maxlength="320" show-word-limit />
          </el-form-item>
          <el-form-item label="主视觉图片" prop="heroImage" class="md:col-span-2">
            <el-input v-model="form.heroImage" placeholder="https://example.com/service-line-hero.jpg" />
          </el-form-item>
        </div>
      </el-card>

      <el-card shadow="never" class="rounded-3xl border border-slate-200">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">Hero 标签</h3>
              <p class="m-0 mt-1 text-sm text-mist">对应 `heroTags`，前台通常显示 3 到 6 个能力标签。</p>
            </div>
            <el-button size="small" :disabled="form.heroTags.length >= 6" @click="addHeroTag">新增标签</el-button>
          </div>
        </template>

        <div class="grid gap-4 xl:grid-cols-3">
          <div v-for="(item, index) in form.heroTags" :key="item.id" class="rounded-2xl border border-slate-200 p-4">
            <div class="mb-3 flex items-center justify-between gap-3">
              <p class="m-0 text-sm font-semibold text-slate-900">标签 {{ index + 1 }}</p>
              <el-button size="small" type="danger" plain :disabled="form.heroTags.length <= 1" @click="removeItem(form.heroTags, index)">
                删除
              </el-button>
            </div>
            <el-input v-model="item.value" placeholder="例如：全程追踪" maxlength="24" show-word-limit />
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="rounded-3xl border border-slate-200">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">关键指标</h3>
              <p class="m-0 mt-1 text-sm text-mist">对应 `metrics`，适合维护方案类型、服务特征、末端覆盖范围等摘要信息。</p>
            </div>
            <el-button size="small" :disabled="form.metrics.length >= 6" @click="addMetric">新增指标</el-button>
          </div>
        </template>

        <div class="grid gap-4 xl:grid-cols-3">
          <div v-for="(item, index) in form.metrics" :key="item.id" class="rounded-2xl border border-slate-200 p-4">
            <div class="mb-3 flex items-center justify-between gap-3">
              <p class="m-0 text-sm font-semibold text-slate-900">指标 {{ index + 1 }}</p>
              <el-button size="small" type="danger" plain :disabled="form.metrics.length <= 1" @click="removeItem(form.metrics, index)">
                删除
              </el-button>
            </div>
            <div class="space-y-3">
              <el-input v-model="item.label" placeholder="例如：常用方案" maxlength="20" show-word-limit />
              <el-input v-model="item.value" placeholder="例如：空运 / 海运" maxlength="40" show-word-limit />
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="rounded-3xl border border-slate-200">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">亮点模块</h3>
              <p class="m-0 mt-1 text-sm text-mist">对应 `highlightsTitle`、`highlightsSubtitle` 和 `highlights`。</p>
            </div>
            <el-button size="small" :disabled="form.highlights.length >= 6" @click="addHighlight">新增亮点</el-button>
          </div>
        </template>

        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="模块标题" prop="highlightsTitle">
            <el-input v-model="form.highlightsTitle" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题" prop="highlightsSubtitle">
            <el-input v-model="form.highlightsSubtitle" maxlength="160" show-word-limit />
          </el-form-item>
        </div>

        <div class="grid gap-4 xl:grid-cols-3">
          <div v-for="(item, index) in form.highlights" :key="item.id" class="rounded-2xl border border-slate-200 p-4">
            <div class="mb-3 flex items-center justify-between gap-3">
              <p class="m-0 text-sm font-semibold text-slate-900">亮点 {{ index + 1 }}</p>
              <el-button size="small" type="danger" plain :disabled="form.highlights.length <= 1" @click="removeItem(form.highlights, index)">
                删除
              </el-button>
            </div>
            <div class="space-y-3">
              <el-input v-model="item.icon" placeholder="mdi-clock-outline" />
              <el-input v-model="item.title" placeholder="亮点标题" maxlength="40" show-word-limit />
              <el-input v-model="item.description" type="textarea" :rows="4" placeholder="亮点描述" maxlength="200" show-word-limit />
            </div>
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="rounded-3xl border border-slate-200">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">服务流程</h3>
              <p class="m-0 mt-1 text-sm text-mist">对应 `processTitle`、`processSubtitle` 和字符串数组 `processSteps`。</p>
            </div>
            <el-button size="small" :disabled="form.processSteps.length >= 8" @click="addProcessStep">新增步骤</el-button>
          </div>
        </template>

        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="模块标题" prop="processTitle">
            <el-input v-model="form.processTitle" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="模块副标题" prop="processSubtitle">
            <el-input v-model="form.processSubtitle" maxlength="160" show-word-limit />
          </el-form-item>
        </div>

        <div class="grid gap-4 xl:grid-cols-3">
          <div v-for="(item, index) in form.processSteps" :key="item.id" class="rounded-2xl border border-slate-200 p-4">
            <div class="mb-3 flex items-center justify-between gap-3">
              <p class="m-0 text-sm font-semibold text-slate-900">步骤 {{ index + 1 }}</p>
              <el-button size="small" type="danger" plain :disabled="form.processSteps.length <= 1" @click="removeItem(form.processSteps, index)">
                删除
              </el-button>
            </div>
            <el-input v-model="item.value" placeholder="例如：咨询报价" maxlength="40" show-word-limit />
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="rounded-3xl border border-slate-200">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">承运范围</h3>
              <p class="m-0 mt-1 text-sm text-mist">对应 `scopeTitle`、`scopeSubtitle`、`scopeImage` 和 `scopeItems`。</p>
            </div>
            <el-button size="small" :disabled="form.scopeItems.length >= 10" @click="addScopeItem">新增范围项</el-button>
          </div>
        </template>

        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="模块标题" prop="scopeTitle">
            <el-input v-model="form.scopeTitle" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="范围配图" prop="scopeImage">
            <el-input v-model="form.scopeImage" placeholder="https://example.com/scope.jpg" />
          </el-form-item>
          <el-form-item label="模块副标题" prop="scopeSubtitle" class="md:col-span-2">
            <el-input v-model="form.scopeSubtitle" type="textarea" :rows="3" maxlength="220" show-word-limit />
          </el-form-item>
        </div>

        <div class="grid gap-4 xl:grid-cols-3">
          <div v-for="(item, index) in form.scopeItems" :key="item.id" class="rounded-2xl border border-slate-200 p-4">
            <div class="mb-3 flex items-center justify-between gap-3">
              <p class="m-0 text-sm font-semibold text-slate-900">范围项 {{ index + 1 }}</p>
              <el-button size="small" type="danger" plain :disabled="form.scopeItems.length <= 1" @click="removeItem(form.scopeItems, index)">
                删除
              </el-button>
            </div>
            <el-input v-model="item.value" placeholder="例如：带电产品" maxlength="60" show-word-limit />
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="rounded-3xl border border-slate-200">
        <template #header>
          <div class="flex items-center justify-between gap-3">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">适配客户与场景</h3>
              <p class="m-0 mt-1 text-sm text-mist">对应 `supportTitle`、`supportDescription` 和 `supportItems`。</p>
            </div>
            <el-button size="small" :disabled="form.supportItems.length >= 8" @click="addSupportItem">新增适配项</el-button>
          </div>
        </template>

        <div class="grid gap-4">
          <el-form-item label="模块标题" prop="supportTitle">
            <el-input v-model="form.supportTitle" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="模块描述" prop="supportDescription">
            <el-input v-model="form.supportDescription" type="textarea" :rows="3" maxlength="220" show-word-limit />
          </el-form-item>
        </div>

        <div class="grid gap-4 xl:grid-cols-3">
          <div v-for="(item, index) in form.supportItems" :key="item.id" class="rounded-2xl border border-slate-200 p-4">
            <div class="mb-3 flex items-center justify-between gap-3">
              <p class="m-0 text-sm font-semibold text-slate-900">适配项 {{ index + 1 }}</p>
              <el-button size="small" type="danger" plain :disabled="form.supportItems.length <= 1" @click="removeItem(form.supportItems, index)">
                删除
              </el-button>
            </div>
            <el-input v-model="item.value" placeholder="例如：跨境电商补货" maxlength="60" show-word-limit />
          </div>
        </div>
      </el-card>

      <el-card shadow="never" class="rounded-3xl border border-slate-200">
        <template #header>
          <div>
            <h3 class="m-0 text-base font-bold text-ink">CTA 引导</h3>
            <p class="m-0 mt-1 text-sm text-mist">对应 `ctaTitle` 和 `ctaSubtitle`。</p>
          </div>
        </template>

        <div class="grid gap-4">
          <el-form-item label="CTA 标题" prop="ctaTitle">
            <el-input v-model="form.ctaTitle" maxlength="80" show-word-limit />
          </el-form-item>
          <el-form-item label="CTA 副标题" prop="ctaSubtitle">
            <el-input v-model="form.ctaSubtitle" type="textarea" :rows="3" maxlength="220" show-word-limit />
          </el-form-item>
        </div>
      </el-card>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchServiceLineContent, publishServiceLine, saveServiceLineDraft } from '../api/service-line'
import {
  createEmptyServiceLineForm,
  createHighlight,
  createMetric,
  createTextItem,
  type ServiceLineCode,
  type ServiceLineDocument,
  type ServiceLineForm,
} from '../types/service-line'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const savingDraft = ref(false)
const publishing = ref(false)
const formRef = ref<FormInstance>()

const document = reactive<ServiceLineDocument>({
  pageCode: '',
  lineCode: 'taiwan',
  lineName: '',
  linePath: '',
  status: 'draft',
  updatedAt: '',
  publishedAt: null,
  form: createEmptyServiceLineForm(),
})

const form = reactive<ServiceLineForm>(createEmptyServiceLineForm())

const rules: FormRules = {
  key: [{ required: true, message: '缺少线路标识', trigger: 'blur' }],
  eyebrow: [{ required: true, message: '请输入 Eyebrow', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  subtitle: [{ required: true, message: '请输入副标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述文案', trigger: 'blur' }],
  heroImage: [{ required: true, message: '请输入主视觉图片', trigger: 'blur' }],
  highlightsTitle: [{ required: true, message: '请输入亮点标题', trigger: 'blur' }],
  processTitle: [{ required: true, message: '请输入流程标题', trigger: 'blur' }],
  scopeTitle: [{ required: true, message: '请输入承运范围标题', trigger: 'blur' }],
  ctaTitle: [{ required: true, message: '请输入 CTA 标题', trigger: 'blur' }],
}

function cloneForm(source: ServiceLineForm): ServiceLineForm {
  return {
    ...source,
    heroTags: source.heroTags.map((item) => ({ ...item })),
    metrics: source.metrics.map((item) => ({ ...item })),
    highlights: source.highlights.map((item) => ({ ...item })),
    processSteps: source.processSteps.map((item) => ({ ...item })),
    scopeItems: source.scopeItems.map((item) => ({ ...item })),
    supportItems: source.supportItems.map((item) => ({ ...item })),
  }
}

function assignForm(target: ServiceLineForm, source: ServiceLineForm) {
  Object.assign(target, {
    ...source,
    heroTags: source.heroTags.map((item) => ({ ...item })),
    metrics: source.metrics.map((item) => ({ ...item })),
    highlights: source.highlights.map((item) => ({ ...item })),
    processSteps: source.processSteps.map((item) => ({ ...item })),
    scopeItems: source.scopeItems.map((item) => ({ ...item })),
    supportItems: source.supportItems.map((item) => ({ ...item })),
  })
}

function applyDocument(nextDocument: ServiceLineDocument) {
  Object.assign(document, {
    ...nextDocument,
    form: cloneForm(nextDocument.form),
  })
  assignForm(form, nextDocument.form)
}

function getCurrentLineCode() {
  return route.params.code as ServiceLineCode | undefined
}

async function loadDocument() {
  const lineCode = getCurrentLineCode()
  if (!lineCode) {
    router.replace('/pages/service-lines')
    return
  }

  loading.value = true
  try {
    const response = await fetchServiceLineContent(lineCode)
    applyDocument(response)
  } catch {
    ElMessage.error('加载线路模板失败')
    router.replace('/pages/service-lines')
  } finally {
    loading.value = false
  }
}

function addHeroTag() {
  form.heroTags.push(createTextItem('hero-tag'))
}

function addMetric() {
  form.metrics.push(createMetric())
}

function addHighlight() {
  form.highlights.push(createHighlight())
}

function addProcessStep() {
  form.processSteps.push(createTextItem('process'))
}

function addScopeItem() {
  form.scopeItems.push(createTextItem('scope'))
}

function addSupportItem() {
  form.supportItems.push(createTextItem('support'))
}

function removeItem<T>(list: T[], index: number) {
  if (list.length <= 1) {
    return
  }
  list.splice(index, 1)
}

function validateItems() {
  if (form.heroTags.some((item) => !item.value.trim())) {
    ElMessage.error('请完整填写 Hero 标签')
    return false
  }
  if (form.metrics.some((item) => !item.label.trim() || !item.value.trim())) {
    ElMessage.error('请完整填写关键指标')
    return false
  }
  if (form.highlights.some((item) => !item.title.trim() || !item.description.trim())) {
    ElMessage.error('请完整填写亮点模块')
    return false
  }
  if (form.processSteps.some((item) => !item.value.trim())) {
    ElMessage.error('请完整填写流程步骤')
    return false
  }
  if (form.scopeItems.some((item) => !item.value.trim())) {
    ElMessage.error('请完整填写承运范围项')
    return false
  }
  if (form.supportItems.some((item) => !item.value.trim())) {
    ElMessage.error('请完整填写适配客户项')
    return false
  }
  return true
}

async function persist(mode: 'draft' | 'publish') {
  const lineCode = getCurrentLineCode()
  if (!lineCode) {
    return
  }

  if (mode === 'publish') {
    const valid = formRef.value
      ? await formRef.value.validate().then(() => true).catch(() => false)
      : true
    if (!valid || !validateItems()) {
      return
    }
  }

  try {
    const payload = cloneForm(form)
    const response = mode === 'draft'
      ? await saveServiceLineDraft(lineCode, payload)
      : await publishServiceLine(lineCode, payload)
    applyDocument(response)
    ElMessage.success(mode === 'draft' ? '线路模板草稿已保存' : '线路模板已发布')
  } catch {
    ElMessage.error(mode === 'draft' ? '保存线路模板草稿失败' : '发布线路模板失败')
  }
}

async function onSaveDraft() {
  savingDraft.value = true
  try {
    await persist('draft')
  } finally {
    savingDraft.value = false
  }
}

async function onPublish() {
  publishing.value = true
  try {
    await persist('publish')
  } finally {
    publishing.value = false
  }
}

watch(
  () => route.params.code,
  () => {
    loadDocument()
  },
  { immediate: true },
)
</script>
