<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <div class="flex items-center gap-3">
          <h2 class="m-0 text-xl font-extrabold text-ink">首页内容管理</h2>
          <el-tag :type="statusTagType" effect="light">{{ statusLabel }}</el-tag>
        </div>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          当前页面用于维护官网首页核心内容，现阶段只保留表单配置区，不再渲染右侧预览模块。
        </p>
        <div class="flex flex-wrap gap-4 text-xs text-mist">
          <span>最近更新：{{ documentMeta.updatedAt || '未保存' }}</span>
          <span>最近发布：{{ documentMeta.publishedAt || '未发布' }}</span>
        </div>
      </div>
      <div class="flex flex-wrap gap-3">
        <el-button :disabled="loading || !canEdit" @click="onReset">重置</el-button>
        <el-button v-if="canEdit" :loading="saving" @click="onSaveDraft">保存草稿</el-button>
        <el-button v-if="canEdit" type="primary" :loading="publishing" @click="onPublish">发布首页</el-button>
      </div>
    </div>

    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="space-y-6">
          <div class="h-80 rounded-3xl bg-slate-100"></div>
          <div class="h-48 rounded-3xl bg-slate-100"></div>
          <div class="h-80 rounded-3xl bg-slate-100"></div>
          <div class="h-80 rounded-3xl bg-slate-100"></div>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-6">
        <el-card class="rounded-3xl border-0 shadow-panel">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-lg font-bold text-ink">Banner 图片组</h3>
                <p class="m-0 mt-1 text-sm text-mist">支持多张图片上传、预览、删除和设为首图。</p>
              </div>
              <el-switch v-model="form.hero.enabled" :disabled="!canEdit" />
            </div>
          </template>

          <div class="space-y-6">
            <div class="rounded-2xl border border-dashed border-slate-300 bg-slate-50/80 p-4">
              <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div>
                  <p class="m-0 text-sm font-semibold text-slate-900">图片组操作</p>
                  <p class="m-0 mt-1 text-sm text-mist">首图将作为前台 Banner 背景图。</p>
                </div>
                <el-upload
                  :auto-upload="false"
                  :show-file-list="false"
                  :multiple="true"
                  accept="image/*"
                  :disabled="!canEdit"
                  :on-change="onBannerFileChange"
                >
                  <el-button type="primary" :disabled="!canEdit">上传 Banner 图片</el-button>
                </el-upload>
              </div>
            </div>

            <div v-if="form.hero.images.length > 0" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              <div
                v-for="(image, index) in form.hero.images"
                :key="image.id"
                class="overflow-hidden rounded-2xl border border-slate-200 bg-white"
              >
                <div class="relative aspect-[16/10] bg-slate-100">
                  <img :src="image.url" :alt="image.name" class="h-full w-full object-cover" />
                  <div
                    v-if="index === 0"
                    class="absolute left-3 top-3 rounded-full bg-brand px-3 py-1 text-xs font-semibold text-white"
                  >
                    当前首图
                  </div>
                </div>
                <div class="space-y-3 p-4">
                  <div>
                    <p class="m-0 truncate text-sm font-semibold text-slate-900">{{ image.name }}</p>
                    <p class="m-0 mt-1 text-xs text-mist">位置：第 {{ index + 1 }} 张</p>
                  </div>
                  <div class="flex flex-wrap gap-2">
                    <el-button size="small" @click="openPreview('Banner 图片预览', image.url)">预览</el-button>
                    <el-button v-if="index > 0 && canEdit" size="small" @click="onSetAsPrimary(index)">设为首图</el-button>
                    <el-button v-if="canEdit" size="small" type="danger" plain @click="onRemoveBanner(index)">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="请先上传至少一张 Banner 图片" />

            <div class="grid gap-4 md:grid-cols-2">
              <el-form-item label="主标题" prop="hero.title" class="md:col-span-2">
                <el-input v-model="form.hero.title" type="textarea" :rows="2" maxlength="60" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="次要内容" prop="hero.subtitle" class="md:col-span-2">
                <el-input v-model="form.hero.subtitle" type="textarea" :rows="3" maxlength="120" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="按钮一名称" prop="hero.primaryButtonText">
                <el-input v-model="form.hero.primaryButtonText" :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="按钮一链接" prop="hero.primaryButtonLink">
                <el-input v-model="form.hero.primaryButtonLink" placeholder="/contact" :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="按钮二名称" prop="hero.secondaryButtonText">
                <el-input v-model="form.hero.secondaryButtonText" :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="按钮二链接" prop="hero.secondaryButtonLink">
                <el-input v-model="form.hero.secondaryButtonLink" placeholder="/services" :disabled="!canEdit" />
              </el-form-item>
            </div>
          </div>
        </el-card>

        <el-card class="rounded-3xl border-0 shadow-panel">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-lg font-bold text-ink">运单查询</h3>
                <p class="m-0 mt-1 text-sm text-mist">显示在 Banner 下方，通过开关控制显示。</p>
              </div>
              <el-switch v-model="form.tracking.enabled" :disabled="!canEdit" />
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="模块标题" prop="tracking.title" class="md:col-span-2">
              <el-input v-model="form.tracking.title" maxlength="20" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="输入框占位文案" prop="tracking.placeholder">
              <el-input v-model="form.tracking.placeholder" maxlength="30" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="查询按钮名称" prop="tracking.buttonText">
              <el-input v-model="form.tracking.buttonText" maxlength="10" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="空状态提示" prop="tracking.emptyText">
              <el-input v-model="form.tracking.emptyText" maxlength="40" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="未查到提示" prop="tracking.notFoundText">
              <el-input v-model="form.tracking.notFoundText" maxlength="40" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="加载提示" prop="tracking.loadingText">
              <el-input v-model="form.tracking.loadingText" maxlength="40" show-word-limit :disabled="!canEdit" />
            </el-form-item>
          </div>
        </el-card>

        <el-card class="rounded-3xl border-0 shadow-panel">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-lg font-bold text-ink">主营业务</h3>
                <p class="m-0 mt-1 text-sm text-mist">支持修改标题、描述和多个业务模块，一行最多显示三个。</p>
              </div>
              <el-switch v-model="form.servicesSection.enabled" :disabled="!canEdit" />
            </div>
          </template>

          <div class="space-y-6">
            <div class="grid gap-4 md:grid-cols-2">
              <el-form-item label="模块标题" prop="servicesSection.title">
                <el-input v-model="form.servicesSection.title" maxlength="30" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="模块描述" prop="servicesSection.description">
                <el-input v-model="form.servicesSection.description" type="textarea" :rows="3" maxlength="120" show-word-limit :disabled="!canEdit" />
              </el-form-item>
            </div>

            <div class="flex items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
              <div>
                <p class="m-0 text-sm font-semibold text-slate-900">业务模块</p>
                <p class="m-0 mt-1 text-sm text-mist">每个业务卡片独立维护图标、名称、描述和跳转路由。</p>
              </div>
              <el-button type="primary" :disabled="!canEdit" @click="onAddService">新增业务</el-button>
            </div>

            <div v-if="form.servicesSection.items.length > 0" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              <div
                v-for="(item, index) in form.servicesSection.items"
                :key="item.id"
                class="space-y-4 rounded-2xl border border-slate-200 bg-white p-4"
              >
                <div class="flex items-center justify-between gap-3">
                  <div>
                    <p class="m-0 text-sm font-semibold text-slate-900">业务 {{ index + 1 }}</p>
                    <p class="m-0 mt-1 text-xs text-mist">用于前台主营业务卡片。</p>
                  </div>
                  <el-button v-if="canEdit" size="small" type="danger" plain @click="onRemoveService(index)">删除</el-button>
                </div>

                <div class="space-y-3 rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-3">
                  <div class="flex items-center gap-3">
                    <div class="flex h-14 w-14 items-center justify-center overflow-hidden rounded-2xl bg-white">
                      <img v-if="item.iconUrl" :src="item.iconUrl" alt="业务图标" class="h-full w-full object-cover" />
                      <span v-else class="text-xs text-mist">无图标</span>
                    </div>
                    <el-upload
                      :auto-upload="false"
                      :show-file-list="false"
                      accept="image/*"
                      :disabled="!canEdit"
                      :on-change="(file: UploadFile) => onServiceIconChange(file, index)"
                    >
                      <el-button size="small" :disabled="!canEdit">上传图标</el-button>
                    </el-upload>
                  </div>
                  <el-button v-if="item.iconUrl && canEdit" size="small" plain @click="onRemoveServiceIcon(index)">移除图标</el-button>
                </div>

                <el-form-item :label="`业务名称 ${index + 1}`" :prop="`servicesSection.items.${index}.name`">
                  <el-input v-model="item.name" maxlength="20" show-word-limit :disabled="!canEdit" />
                </el-form-item>
                <el-form-item :label="`业务描述 ${index + 1}`" :prop="`servicesSection.items.${index}.description`">
                  <el-input v-model="item.description" type="textarea" :rows="4" maxlength="120" show-word-limit :disabled="!canEdit" />
                </el-form-item>
                <el-form-item :label="`查看更多路由 ${index + 1}`" :prop="`servicesSection.items.${index}.link`">
                  <el-input v-model="item.link" placeholder="/services/air" :disabled="!canEdit" />
                </el-form-item>
              </div>
            </div>
            <el-empty v-else description="请先添加主营业务模块" />
          </div>
        </el-card>

        <el-card class="rounded-3xl border-0 shadow-panel">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-lg font-bold text-ink">一站式服务</h3>
                <p class="m-0 mt-1 text-sm text-mist">可修改标题、副标题，步骤最多 7 个。</p>
              </div>
              <el-switch v-model="form.processSection.enabled" :disabled="!canEdit" />
            </div>
          </template>

          <div class="space-y-6">
            <div class="grid gap-4 md:grid-cols-2">
              <el-form-item label="模块标题" prop="processSection.title">
                <el-input v-model="form.processSection.title" maxlength="30" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="副标题" prop="processSection.subtitle">
                <el-input v-model="form.processSection.subtitle" type="textarea" :rows="3" maxlength="120" show-word-limit :disabled="!canEdit" />
              </el-form-item>
            </div>

            <div class="flex items-center justify-between gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
              <div>
                <p class="m-0 text-sm font-semibold text-slate-900">服务步骤</p>
                <p class="m-0 mt-1 text-sm text-mist">最多添加 7 个步骤，超出后不可继续新增。</p>
              </div>
              <el-button type="primary" :disabled="!canEdit || form.processSection.steps.length >= 7" @click="onAddProcessStep">
                新增步骤
              </el-button>
            </div>

            <div v-if="form.processSection.steps.length > 0" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              <div
                v-for="(step, index) in form.processSection.steps"
                :key="step.id"
                class="space-y-4 rounded-2xl border border-slate-200 bg-white p-4"
              >
                <div class="flex items-center justify-between gap-3">
                  <div>
                    <p class="m-0 text-sm font-semibold text-slate-900">步骤 {{ index + 1 }}</p>
                    <p class="m-0 mt-1 text-xs text-mist">按前台展示顺序填写。</p>
                  </div>
                  <el-button v-if="canEdit" size="small" type="danger" plain @click="onRemoveProcessStep(index)">删除</el-button>
                </div>
                <el-form-item :label="`步骤标题 ${index + 1}`" :prop="`processSection.steps.${index}.title`">
                  <el-input v-model="step.title" maxlength="20" show-word-limit :disabled="!canEdit" />
                </el-form-item>
                <el-form-item :label="`步骤描述 ${index + 1}`" :prop="`processSection.steps.${index}.description`">
                  <el-input v-model="step.description" type="textarea" :rows="4" maxlength="120" show-word-limit :disabled="!canEdit" />
                </el-form-item>
              </div>
            </div>
            <el-empty v-else description="请先添加服务流程步骤" />
          </div>
        </el-card>

        <el-card class="rounded-3xl border-0 shadow-panel">
          <template #header>
            <div>
              <h3 class="m-0 text-lg font-bold text-ink">我们承诺</h3>
              <p class="m-0 mt-1 text-sm text-mist">固定 6 个模块，每个模块维护图标、标题和子标题描述。</p>
            </div>
          </template>

          <div class="space-y-6">
            <div class="grid gap-4 md:grid-cols-2">
              <el-form-item label="模块标题" prop="promiseSection.title">
                <el-input v-model="form.promiseSection.title" maxlength="30" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="模块副标题" prop="promiseSection.subtitle">
                <el-input v-model="form.promiseSection.subtitle" type="textarea" :rows="3" maxlength="120" show-word-limit :disabled="!canEdit" />
              </el-form-item>
            </div>

            <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              <div
                v-for="(item, index) in form.promiseSection.items"
                :key="item.id"
                class="space-y-4 rounded-2xl border border-slate-200 bg-white p-4"
              >
                <div>
                  <p class="m-0 text-sm font-semibold text-slate-900">承诺模块 {{ index + 1 }}</p>
                  <p class="m-0 mt-1 text-xs text-mist">固定位置，不支持新增或删除。</p>
                </div>

                <div class="space-y-3 rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-3">
                  <div class="flex items-center gap-3">
                    <div class="flex h-14 w-14 items-center justify-center overflow-hidden rounded-2xl bg-white">
                      <img v-if="item.iconUrl" :src="item.iconUrl" alt="承诺图标" class="h-full w-full object-cover" />
                      <span v-else class="text-xs text-mist">无图标</span>
                    </div>
                    <el-upload
                      :auto-upload="false"
                      :show-file-list="false"
                      accept="image/*"
                      :disabled="!canEdit"
                      :on-change="(file: UploadFile) => onPromiseIconChange(file, index)"
                    >
                      <el-button size="small" :disabled="!canEdit">上传图标</el-button>
                    </el-upload>
                  </div>
                  <el-button v-if="item.iconUrl && canEdit" size="small" plain @click="onRemovePromiseIcon(index)">移除图标</el-button>
                </div>

                <el-form-item :label="`标题 ${index + 1}`" :prop="`promiseSection.items.${index}.title`">
                  <el-input v-model="item.title" maxlength="20" show-word-limit :disabled="!canEdit" />
                </el-form-item>
                <el-form-item :label="`子标题描述 ${index + 1}`" :prop="`promiseSection.items.${index}.subtitle`">
                  <el-input v-model="item.subtitle" type="textarea" :rows="4" maxlength="120" show-word-limit :disabled="!canEdit" />
                </el-form-item>
                <el-form-item :label="`配图链接 ${index + 1}`" :prop="`promiseSection.items.${index}.imageUrl`">
                  <el-input v-model="item.imageUrl" placeholder="https://example.com/promise.jpg" :disabled="!canEdit" />
                </el-form-item>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="rounded-3xl border-0 shadow-panel">
          <template #header>
            <div>
              <h3 class="m-0 text-lg font-bold text-ink">新闻预览模块</h3>
              <p class="m-0 mt-1 text-sm text-mist">维护首页新闻预览区标题、副标题和查看全部按钮。</p>
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="模块标题" prop="newsPreviewSection.title">
              <el-input v-model="form.newsPreviewSection.title" maxlength="30" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="模块副标题" prop="newsPreviewSection.subtitle">
              <el-input v-model="form.newsPreviewSection.subtitle" maxlength="120" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="查看全部文案" prop="newsPreviewSection.viewAllText">
              <el-input v-model="form.newsPreviewSection.viewAllText" maxlength="20" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="查看全部链接" prop="newsPreviewSection.viewAllUrl">
              <el-input v-model="form.newsPreviewSection.viewAllUrl" placeholder="/news" :disabled="!canEdit" />
            </el-form-item>
          </div>
        </el-card>

        <el-card class="rounded-3xl border-0 shadow-panel">
          <template #header>
            <div>
              <h3 class="m-0 text-lg font-bold text-ink">SEO 设置</h3>
              <p class="m-0 mt-1 text-sm text-mist">维护页面标题、描述和关键词。</p>
            </div>
          </template>

          <div class="space-y-4">
            <el-form-item label="SEO 标题" prop="seo.title">
              <el-input v-model="form.seo.title" maxlength="70" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="SEO 描述" prop="seo.description">
              <el-input v-model="form.seo.description" type="textarea" :rows="4" maxlength="160" show-word-limit :disabled="!canEdit" />
            </el-form-item>
            <el-form-item label="SEO 关键词" prop="seo.keywords">
              <el-input v-model="form.seo.keywords" type="textarea" :rows="3" maxlength="160" show-word-limit :disabled="!canEdit" />
            </el-form-item>
          </div>
        </el-card>
      </el-form>
    </el-skeleton>

    <el-dialog v-model="previewDialogVisible" :title="previewDialogTitle" width="720px" destroy-on-close>
      <img :src="previewDialogImage" alt="预览图" class="max-h-[70vh] w-full rounded-2xl object-contain" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules, UploadFile, UploadFiles } from 'element-plus'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchHomeContent, publishHomeContent, saveHomeContentDraft } from '../api/content'
import { useAuthStore } from '../stores/auth'
import type {
  HomeBannerImage,
  HomeContentDocument,
  HomeContentForm,
  HomeProcessStepItem,
  HomePromiseItem,
  HomeServiceItem,
} from '../types/content'

const auth = useAuthStore()
const canEdit = computed(() => Boolean(auth.me))

const formRef = ref<FormInstance>()
const loading = ref(true)
const saving = ref(false)
const publishing = ref(false)
const previewDialogVisible = ref(false)
const previewDialogTitle = ref('')
const previewDialogImage = ref('')

const documentMeta = reactive<Pick<HomeContentDocument, 'status' | 'updatedAt' | 'publishedAt'>>({
  status: 'draft',
  updatedAt: '',
  publishedAt: null,
})

function createId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function createBannerId() {
  return createId('banner')
}

function createEmptyServiceItem(): HomeServiceItem {
  return {
    id: createId('service'),
    iconUrl: '',
    name: '',
    description: '',
    link: '',
  }
}

function createEmptyProcessStep(): HomeProcessStepItem {
  return {
    id: createId('process'),
    title: '',
    description: '',
  }
}

function createPromiseItem(id: string): HomePromiseItem {
  return {
    id,
    iconUrl: '',
    title: '',
    subtitle: '',
    imageUrl: '',
  }
}

function createDefaultPromiseItems(): HomePromiseItem[] {
  return [
    createPromiseItem('promise-1'),
    createPromiseItem('promise-2'),
    createPromiseItem('promise-3'),
    createPromiseItem('promise-4'),
    createPromiseItem('promise-5'),
    createPromiseItem('promise-6'),
  ]
}

const form = reactive<HomeContentForm>({
  hero: {
    enabled: true,
    images: [],
    title: '',
    subtitle: '',
    primaryButtonText: '',
    primaryButtonLink: '',
    secondaryButtonText: '',
    secondaryButtonLink: '',
  },
  tracking: {
    enabled: true,
    title: '',
    placeholder: '',
    buttonText: '',
    emptyText: '',
    notFoundText: '',
    loadingText: '',
  },
  servicesSection: {
    enabled: true,
    title: '',
    description: '',
    items: [],
  },
  processSection: {
    enabled: true,
    title: '',
    subtitle: '',
    steps: [],
  },
  promiseSection: {
    title: '',
    subtitle: '',
    items: createDefaultPromiseItems(),
  },
  newsPreviewSection: {
    title: '',
    subtitle: '',
    viewAllText: '',
    viewAllUrl: '',
  },
  seo: {
    title: '',
    description: '',
    keywords: '',
  },
})

const rules: FormRules = {
  'hero.title': [{ required: true, message: '请输入 Banner 主标题', trigger: 'blur' }],
  'hero.subtitle': [{ required: true, message: '请输入 Banner 次要内容', trigger: 'blur' }],
  'hero.primaryButtonText': [{ required: true, message: '请输入按钮一名称', trigger: 'blur' }],
  'hero.primaryButtonLink': [{ required: true, message: '请输入按钮一链接', trigger: 'blur' }],
  'hero.secondaryButtonText': [{ required: true, message: '请输入按钮二名称', trigger: 'blur' }],
  'hero.secondaryButtonLink': [{ required: true, message: '请输入按钮二链接', trigger: 'blur' }],
  'tracking.title': [{ required: true, message: '请输入运单查询标题', trigger: 'blur' }],
  'tracking.placeholder': [{ required: true, message: '请输入运单号占位文案', trigger: 'blur' }],
  'tracking.buttonText': [{ required: true, message: '请输入查询按钮名称', trigger: 'blur' }],
  'tracking.notFoundText': [{ required: true, message: '请输入未查到运单提示', trigger: 'blur' }],
  'tracking.loadingText': [{ required: true, message: '请输入加载提示文案', trigger: 'blur' }],
  'servicesSection.title': [{ required: true, message: '请输入主营业务标题', trigger: 'blur' }],
  'servicesSection.description': [{ required: true, message: '请输入主营业务描述', trigger: 'blur' }],
  'processSection.title': [{ required: true, message: '请输入一站式服务标题', trigger: 'blur' }],
  'processSection.subtitle': [{ required: true, message: '请输入一站式服务副标题', trigger: 'blur' }],
  'promiseSection.title': [{ required: true, message: '请输入我们承诺标题', trigger: 'blur' }],
  'promiseSection.subtitle': [{ required: true, message: '请输入我们承诺副标题', trigger: 'blur' }],
  'newsPreviewSection.title': [{ required: true, message: '请输入新闻预览标题', trigger: 'blur' }],
  'newsPreviewSection.viewAllText': [{ required: true, message: '请输入查看全部按钮文案', trigger: 'blur' }],
  'newsPreviewSection.viewAllUrl': [{ required: true, message: '请输入查看全部链接', trigger: 'blur' }],
  'seo.title': [{ required: true, message: '请输入 SEO 标题', trigger: 'blur' }],
  'seo.description': [{ required: true, message: '请输入 SEO 描述', trigger: 'blur' }],
  'seo.keywords': [{ required: true, message: '请输入 SEO 关键词', trigger: 'blur' }],
}

const statusLabel = computed(() => (documentMeta.status === 'published' ? '已发布' : '草稿'))
const statusTagType = computed(() => (documentMeta.status === 'published' ? 'success' : 'warning'))

function cloneForm(source: HomeContentForm): HomeContentForm {
  return JSON.parse(JSON.stringify(source)) as HomeContentForm
}

function normalizePromiseItems(items: unknown): HomePromiseItem[] {
  const source = Array.isArray(items) ? items : []
  const defaults = createDefaultPromiseItems()
  const normalized = defaults.map((fallback, index) => {
    const current = source[index] as Partial<HomePromiseItem> | undefined
    return {
      id: current?.id || fallback.id,
      iconUrl: current?.iconUrl || '',
      title: current?.title || '',
      subtitle: current?.subtitle || '',
      imageUrl: current?.imageUrl || '',
    }
  })
  return normalized
}

function replaceImages(images: HomeBannerImage[]) {
  form.hero.images.splice(0, form.hero.images.length, ...images.map((image) => ({ ...image })))
}

function replaceServices(items: HomeServiceItem[]) {
  form.servicesSection.items.splice(0, form.servicesSection.items.length, ...items.map((item) => ({ ...item })))
}

function replaceProcessSteps(steps: HomeProcessStepItem[]) {
  form.processSection.steps.splice(0, form.processSection.steps.length, ...steps.map((step) => ({ ...step })))
}

function replacePromiseItems(items: HomePromiseItem[]) {
  form.promiseSection.items.splice(0, form.promiseSection.items.length, ...items.map((item) => ({ ...item })))
}

function applyForm(nextForm: Partial<HomeContentForm>) {
  const nextHero = nextForm.hero ?? form.hero
  const nextTracking = nextForm.tracking ?? form.tracking
  const nextServices = nextForm.servicesSection ?? form.servicesSection
  const nextProcess = nextForm.processSection ?? form.processSection
  const nextPromise = nextForm.promiseSection ?? form.promiseSection
  const nextNewsPreview = nextForm.newsPreviewSection ?? form.newsPreviewSection
  const nextSeo = nextForm.seo ?? form.seo

  Object.assign(form.hero, {
    enabled: nextHero.enabled ?? true,
    title: nextHero.title ?? '',
    subtitle: nextHero.subtitle ?? '',
    primaryButtonText: nextHero.primaryButtonText ?? '',
    primaryButtonLink: nextHero.primaryButtonLink ?? '',
    secondaryButtonText: nextHero.secondaryButtonText ?? '',
    secondaryButtonLink: nextHero.secondaryButtonLink ?? '',
  })
  replaceImages(Array.isArray(nextHero.images) ? nextHero.images : [])

  Object.assign(form.tracking, {
    enabled: nextTracking.enabled ?? true,
    title: nextTracking.title ?? '',
    placeholder: nextTracking.placeholder ?? '',
    buttonText: nextTracking.buttonText ?? '',
    emptyText: nextTracking.emptyText ?? '',
    notFoundText: nextTracking.notFoundText ?? '',
    loadingText: nextTracking.loadingText ?? '',
  })

  Object.assign(form.servicesSection, {
    enabled: nextServices.enabled ?? true,
    title: nextServices.title ?? '',
    description: nextServices.description ?? '',
  })
  replaceServices(Array.isArray(nextServices.items) ? nextServices.items : [])

  Object.assign(form.processSection, {
    enabled: nextProcess.enabled ?? true,
    title: nextProcess.title ?? '',
    subtitle: nextProcess.subtitle ?? '',
  })
  replaceProcessSteps(Array.isArray(nextProcess.steps) ? nextProcess.steps : [])

  Object.assign(form.promiseSection, {
    title: nextPromise.title ?? '',
    subtitle: nextPromise.subtitle ?? '',
  })
  replacePromiseItems(normalizePromiseItems(nextPromise.items))

  Object.assign(form.newsPreviewSection, {
    title: nextNewsPreview.title ?? '',
    subtitle: nextNewsPreview.subtitle ?? '',
    viewAllText: nextNewsPreview.viewAllText ?? '',
    viewAllUrl: nextNewsPreview.viewAllUrl ?? '',
  })

  Object.assign(form.seo, {
    title: nextSeo.title ?? '',
    description: nextSeo.description ?? '',
    keywords: nextSeo.keywords ?? '',
  })
}

function applyDocument(document: HomeContentDocument) {
  documentMeta.status = document.status
  documentMeta.updatedAt = document.updatedAt
  documentMeta.publishedAt = document.publishedAt
  applyForm(document.form)
}

async function readFileAsDataUrl(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '')
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}

async function loadContent() {
  loading.value = true
  try {
    const document = await fetchHomeContent()
    applyDocument(document)
  } finally {
    loading.value = false
  }
}

async function validateForm() {
  const valid = formRef.value
    ? await formRef.value.validate().then(() => true).catch(() => false)
    : true

  if (!valid) {
    return false
  }

  if (form.hero.enabled && form.hero.images.length === 0) {
    ElMessage.error('请至少上传一张 Banner 图片')
    return false
  }

  if (form.servicesSection.enabled) {
    if (form.servicesSection.items.length === 0) {
      ElMessage.error('请至少添加一个主营业务模块')
      return false
    }

    const invalidService = form.servicesSection.items.some((item) => !item.name.trim() || !item.description.trim() || !item.link.trim())
    if (invalidService) {
      ElMessage.error('请完整填写主营业务模块的名称、描述和路由')
      return false
    }
  }

  if (form.processSection.enabled) {
    if (form.processSection.steps.length === 0) {
      ElMessage.error('请至少添加一个一站式服务步骤')
      return false
    }

    if (form.processSection.steps.length > 7) {
      ElMessage.error('一站式服务步骤最多只能添加 7 个')
      return false
    }

    const invalidStep = form.processSection.steps.some((step) => !step.title.trim() || !step.description.trim())
    if (invalidStep) {
      ElMessage.error('请完整填写一站式服务步骤的标题和描述')
      return false
    }
  }

  const invalidPromiseItem = form.promiseSection.items.some((item) => !item.title.trim() || !item.subtitle.trim())
  if (invalidPromiseItem) {
    ElMessage.error('请完整填写我们承诺的 6 个固定模块')
    return false
  }

  return true
}

function openPreview(title: string, imageUrl: string) {
  previewDialogTitle.value = title
  previewDialogImage.value = imageUrl
  previewDialogVisible.value = true
}

async function onBannerFileChange(uploadFile: UploadFile, _uploadFiles: UploadFiles) {
  if (!uploadFile.raw) {
    return
  }

  const url = await readFileAsDataUrl(uploadFile.raw)
  form.hero.images.push({
    id: createBannerId(),
    name: uploadFile.name,
    url,
  })
}

function onRemoveBanner(index: number) {
  form.hero.images.splice(index, 1)
}

function onSetAsPrimary(index: number) {
  const [selected] = form.hero.images.splice(index, 1)
  if (selected) {
    form.hero.images.unshift(selected)
  }
}

async function onServiceIconChange(uploadFile: UploadFile, index: number) {
  if (!uploadFile.raw || !form.servicesSection.items[index]) {
    return
  }

  form.servicesSection.items[index].iconUrl = await readFileAsDataUrl(uploadFile.raw)
}

function onRemoveServiceIcon(index: number) {
  if (form.servicesSection.items[index]) {
    form.servicesSection.items[index].iconUrl = ''
  }
}

function onAddService() {
  form.servicesSection.items.push(createEmptyServiceItem())
}

function onRemoveService(index: number) {
  form.servicesSection.items.splice(index, 1)
}

function onAddProcessStep() {
  if (form.processSection.steps.length >= 7) {
    ElMessage.warning('一站式服务步骤最多只能添加 7 个')
    return
  }

  form.processSection.steps.push(createEmptyProcessStep())
}

function onRemoveProcessStep(index: number) {
  form.processSection.steps.splice(index, 1)
}

async function onPromiseIconChange(uploadFile: UploadFile, index: number) {
  if (!uploadFile.raw || !form.promiseSection.items[index]) {
    return
  }

  form.promiseSection.items[index].iconUrl = await readFileAsDataUrl(uploadFile.raw)
}

function onRemovePromiseIcon(index: number) {
  if (form.promiseSection.items[index]) {
    form.promiseSection.items[index].iconUrl = ''
  }
}

async function onSaveDraft() {
  if (!(await validateForm())) {
    return
  }

  saving.value = true
  try {
    const document = await saveHomeContentDraft(cloneForm(form))
    applyDocument(document)
    ElMessage.success('首页内容草稿已保存')
  } catch (error) {
    ElMessage.error('保存草稿失败')
  } finally {
    saving.value = false
  }
}

async function onPublish() {
  if (!(await validateForm())) {
    return
  }

  publishing.value = true
  try {
    const document = await publishHomeContent(cloneForm(form))
    applyDocument(document)
    ElMessage.success('首页内容已发布')
  } catch (error) {
    ElMessage.error('发布首页失败')
  } finally {
    publishing.value = false
  }
}

async function onReset() {
  await loadContent()
  ElMessage.success('已恢复到最近保存的内容')
}

onMounted(() => {
  loadContent()
})
</script>
