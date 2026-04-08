<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <div class="flex items-center gap-3">
          <h2 class="m-0 text-xl font-extrabold text-ink">首页内容管理</h2>
          <el-tag :type="statusTagType" effect="light">{{ statusLabel }}</el-tag>
        </div>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">当前页面用于维护官网首页核心内容，包含 Banner 图片组、运单追踪、主营业务、联系转化区和 SEO 信息。</p>
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
        <div class="grid gap-6 xl:grid-cols-[minmax(0,1.65fr)_400px]">
          <div class="space-y-6">
            <div class="h-80 rounded-3xl bg-slate-100"></div>
            <div class="h-44 rounded-3xl bg-slate-100"></div>
            <div class="h-96 rounded-3xl bg-slate-100"></div>
          </div>
          <div class="space-y-6">
            <div class="h-96 rounded-3xl bg-slate-100"></div>
            <div class="h-80 rounded-3xl bg-slate-100"></div>
          </div>
        </div>
      </template>

      <div class="grid gap-6 xl:grid-cols-[minmax(0,1.65fr)_400px]">
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
                    <p class="m-0 mt-1 text-sm text-mist">建议使用宽图。首图将作为首页 Banner 背景图。</p>
                  </div>
                  <el-upload :auto-upload="false" :show-file-list="false" :multiple="true" accept="image/*" :disabled="!canEdit" :on-change="onBannerFileChange">
                    <el-button type="primary" :disabled="!canEdit">上传 Banner 图片</el-button>
                  </el-upload>
                </div>
              </div>

              <div v-if="form.hero.images.length > 0" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                <div v-for="(image, index) in form.hero.images" :key="image.id" class="overflow-hidden rounded-2xl border border-slate-200 bg-white">
                  <div class="relative aspect-[16/10] bg-slate-100">
                    <img :src="image.url" :alt="image.name" class="h-full w-full object-cover" />
                    <div v-if="index === 0" class="absolute left-3 top-3 rounded-full bg-brand px-3 py-1 text-xs font-semibold text-white">当前首图</div>
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
                  <h3 class="m-0 text-lg font-bold text-ink">运单追踪</h3>
                  <p class="m-0 mt-1 text-sm text-mist">显示在 Banner 下方，可通过开关控制展示。</p>
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
            </div>
          </el-card>

          <el-card class="rounded-3xl border-0 shadow-panel">
            <template #header>
              <div class="flex items-center justify-between gap-3">
                <div>
                  <h3 class="m-0 text-lg font-bold text-ink">主营业务</h3>
                  <p class="m-0 mt-1 text-sm text-mist">支持模块标题、描述和多个业务小模块的独立维护。</p>
                </div>
                <el-switch v-model="form.servicesSection.enabled" :disabled="!canEdit" />
              </div>
            </template>
            <div class="space-y-6">
              <div class="grid gap-4 md:grid-cols-2">
                <el-form-item label="模块标题" prop="servicesSection.title">
                  <el-input v-model="form.servicesSection.title" maxlength="20" show-word-limit :disabled="!canEdit" />
                </el-form-item>
                <el-form-item label="模块描述" prop="servicesSection.description">
                  <el-input v-model="form.servicesSection.description" maxlength="80" show-word-limit :disabled="!canEdit" />
                </el-form-item>
              </div>
              <div v-if="canEdit" class="flex justify-end">
                <el-button type="primary" plain @click="onAddService">新增业务模块</el-button>
              </div>
              <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                <div v-for="(item, index) in form.servicesSection.items" :key="item.id" class="rounded-2xl border border-slate-200 bg-slate-50/70 p-4">
                  <div class="mb-4 flex items-center justify-between gap-3">
                    <div>
                      <p class="m-0 text-sm font-semibold text-slate-900">业务模块 {{ index + 1 }}</p>
                      <p class="m-0 mt-1 text-xs text-mist">支持图标、名称、描述和查看更多路由。</p>
                    </div>
                    <el-button v-if="canEdit" size="small" type="danger" plain @click="onRemoveService(index)">删除</el-button>
                  </div>
                  <div class="space-y-4">
                    <div class="rounded-2xl border border-dashed border-slate-300 bg-white p-4">
                      <div class="space-y-4">
                        <div class="flex items-center gap-4">
                          <div class="flex h-16 w-16 items-center justify-center rounded-2xl bg-orange-50">
                            <img v-if="item.iconUrl" :src="item.iconUrl" :alt="item.name || `service-${index + 1}`" class="h-10 w-10 object-contain" />
                            <span v-else class="text-xs font-semibold text-orange-500">ICON</span>
                          </div>
                          <div class="space-y-1 text-sm text-mist">
                            <p class="m-0 font-semibold text-slate-900">业务图标</p>
                            <p class="m-0">建议上传透明底 PNG 或 SVG。</p>
                          </div>
                        </div>
                        <div class="flex flex-wrap gap-2">
                          <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :disabled="!canEdit" :on-change="createServiceIconUploader(index)">
                            <el-button size="small" type="primary" :disabled="!canEdit">上传图标</el-button>
                          </el-upload>
                          <el-button v-if="item.iconUrl" size="small" @click="openPreview('业务图标预览', item.iconUrl)">预览</el-button>
                          <el-button v-if="item.iconUrl && canEdit" size="small" type="danger" plain @click="onRemoveServiceIcon(index)">移除</el-button>
                        </div>
                      </div>
                    </div>
                    <div class="grid gap-4">
                      <el-form-item :label="`业务名称 ${index + 1}`">
                        <el-input v-model="item.name" maxlength="20" show-word-limit :disabled="!canEdit" />
                      </el-form-item>
                      <el-form-item :label="`业务描述 ${index + 1}`">
                        <el-input v-model="item.description" type="textarea" :rows="3" maxlength="120" show-word-limit :disabled="!canEdit" />
                      </el-form-item>
                      <el-form-item :label="`查看更多路由 ${index + 1}`">
                        <el-input v-model="item.link" placeholder="/services/taiwan" :disabled="!canEdit" />
                      </el-form-item>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <el-card class="rounded-3xl border-0 shadow-panel">
            <template #header>
              <div>
                <h3 class="m-0 text-lg font-bold text-ink">联系转化区</h3>
                <p class="m-0 mt-1 text-sm text-mist">用于首页咨询和报价转化，建议保持简洁明确。</p>
              </div>
            </template>
            <div class="grid gap-4 md:grid-cols-2">
              <el-form-item label="模块标题" prop="contact.title" class="md:col-span-2">
                <el-input v-model="form.contact.title" maxlength="40" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="模块描述" prop="contact.description" class="md:col-span-2">
                <el-input v-model="form.contact.description" type="textarea" :rows="3" maxlength="120" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="联系电话" prop="contact.phone">
                <el-input v-model="form.contact.phone" :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="联系邮箱" prop="contact.email">
                <el-input v-model="form.contact.email" :disabled="!canEdit" />
              </el-form-item>
            </div>
          </el-card>

          <el-card class="rounded-3xl border-0 shadow-panel">
            <template #header>
              <div>
                <h3 class="m-0 text-lg font-bold text-ink">SEO 设置</h3>
                <p class="m-0 mt-1 text-sm text-mist">用于搜索标题、描述和关键词维护。</p>
              </div>
            </template>
            <div class="grid gap-4">
              <el-form-item label="SEO 标题" prop="seo.title">
                <el-input v-model="form.seo.title" maxlength="70" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="SEO 描述" prop="seo.description">
                <el-input v-model="form.seo.description" type="textarea" :rows="3" maxlength="160" show-word-limit :disabled="!canEdit" />
              </el-form-item>
              <el-form-item label="SEO 关键词" prop="seo.keywords">
                <el-input v-model="form.seo.keywords" placeholder="关键词之间使用英文逗号分隔" :disabled="!canEdit" />
              </el-form-item>
            </div>
          </el-card>
        </el-form>

        <div class="space-y-6">
          <el-card class="rounded-3xl border-0 shadow-panel">
            <template #header>
              <div>
                <h3 class="m-0 text-lg font-bold text-ink">首页预览摘要</h3>
                <p class="m-0 mt-1 text-sm text-mist">实时反映 Banner、运单追踪和主营业务配置。</p>
              </div>
            </template>
            <div class="space-y-6">
              <div class="overflow-hidden rounded-2xl bg-slate-950 text-white">
                <div class="relative min-h-[360px] bg-cover bg-center p-6 md:p-8" :style="{ backgroundImage: previewBackgroundStyle }">
                  <div class="absolute inset-0 bg-slate-950/45"></div>
                  <div class="relative z-10 flex min-h-[312px] max-w-xl flex-col justify-center">
                    <p v-if="!form.hero.enabled" class="mb-4 inline-flex w-fit rounded-full bg-white/10 px-3 py-1 text-xs font-semibold tracking-[0.18em] text-slate-100">BANNER DISABLED</p>
                    <h4 class="m-0 text-4xl font-black leading-tight">{{ form.hero.title || '未填写主标题' }}</h4>
                    <p class="m-0 mt-4 text-lg leading-8 text-slate-100">{{ form.hero.subtitle || '未填写次要内容' }}</p>
                    <div class="mt-6 flex flex-wrap gap-3">
                      <a class="inline-flex items-center rounded-lg bg-orange-500 px-5 py-3 text-sm font-semibold text-white no-underline" :href="form.hero.primaryButtonLink || '#'">{{ form.hero.primaryButtonText || '按钮一名称' }}</a>
                      <a class="inline-flex items-center rounded-lg bg-white px-5 py-3 text-sm font-semibold text-slate-900 no-underline" :href="form.hero.secondaryButtonLink || '#'">{{ form.hero.secondaryButtonText || '按钮二名称' }}</a>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="form.tracking.enabled" class="rounded-3xl border border-slate-200 bg-white px-5 py-6 shadow-sm">
                <div class="space-y-5">
                  <div class="text-center">
                    <h4 class="m-0 text-2xl font-black text-slate-900">{{ form.tracking.title || '运单追踪' }}</h4>
                  </div>
                  <div class="rounded-2xl border border-slate-100 bg-white p-4 shadow-[0_14px_40px_rgba(15,23,42,0.08)]">
                    <div class="flex flex-col gap-3 md:flex-row">
                      <div class="min-w-0 flex-1 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-400">{{ form.tracking.placeholder || '请输入运单号' }}</div>
                      <div class="rounded-xl bg-orange-500 px-6 py-3 text-center text-sm font-semibold text-white">{{ form.tracking.buttonText || '查询' }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="form.servicesSection.enabled" class="space-y-4 rounded-3xl border border-slate-200 bg-white px-5 py-6">
                <div class="text-center">
                  <h4 class="m-0 text-3xl font-black text-slate-900">{{ form.servicesSection.title || '主营业务' }}</h4>
                  <p class="m-0 mt-3 text-sm leading-6 text-slate-500">{{ form.servicesSection.description || '请填写主营业务模块描述' }}</p>
                </div>
                <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                  <div v-for="item in previewServiceItems" :key="item.id" class="rounded-2xl border border-slate-200 bg-white px-5 py-6">
                    <div class="flex flex-col items-center text-center">
                      <div class="flex h-16 w-16 items-center justify-center rounded-2xl bg-orange-50">
                        <img v-if="item.iconUrl" :src="item.iconUrl" :alt="item.name" class="h-10 w-10 object-contain" />
                        <span v-else class="text-xs font-semibold text-orange-500">ICON</span>
                      </div>
                      <h5 class="m-0 mt-5 text-2xl font-black text-slate-900">{{ item.name || '未填写业务名称' }}</h5>
                      <p class="m-0 mt-4 text-sm leading-7 text-slate-500">{{ item.description || '未填写业务描述' }}</p>
                      <a :href="item.link || '#'" class="mt-5 inline-flex items-center rounded-full border border-slate-200 px-5 py-2 text-sm font-semibold text-slate-700 no-underline">查看更多 -></a>
                    </div>
                  </div>
                </div>
              </div>

              <div class="rounded-2xl bg-slate-50 p-4">
                <div class="flex items-center justify-between gap-3">
                  <p class="m-0 text-sm font-bold text-slate-900">图片组概览</p>
                  <span class="text-xs text-mist">共 {{ form.hero.images.length }} 张</span>
                </div>
                <p class="m-0 mt-2 text-sm leading-6 text-mist">当前首图固定取第 1 张。若需要替换首页 Banner 主图，请在左侧卡片中点击“设为首图”。</p>
              </div>
            </div>
          </el-card>

          <el-card class="rounded-3xl border-0 shadow-panel">
            <template #header><h3 class="m-0 text-lg font-bold text-ink">发布说明</h3></template>
            <ul class="m-0 space-y-3 pl-5 text-sm leading-6 text-mist">
              <li>当前阶段保存和发布仅写入浏览器本地存储，用于先跑通内容维护流程。</li>
              <li>后续接入后端时，优先替换 `src/api/content.ts`，不要推翻现有表单结构。</li>
              <li>发布前至少检查首图、运单追踪开关、主营业务开关和查看更多路由是否完整。</li>
            </ul>
          </el-card>
        </div>
      </div>
    </el-skeleton>

    <el-dialog v-model="previewDialogVisible" width="720px" :title="previewDialogTitle">
      <div class="overflow-hidden rounded-2xl bg-slate-100">
        <img v-if="previewImageUrl" :src="previewImageUrl" alt="preview" class="max-h-[70vh] w-full object-contain" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules, UploadFile, UploadFiles } from 'element-plus'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchHomeContent, publishHomeContent, saveHomeContentDraft } from '../api/content'
import { useAuthStore } from '../stores/auth'
import type { HomeBannerImage, HomeContentDocument, HomeContentForm, HomeServiceItem } from '../types/content'

const auth = useAuthStore()
const canEdit = computed(() => auth.hasPermission('content:edit'))
const formRef = ref<FormInstance>()
const loading = ref(true)
const saving = ref(false)
const publishing = ref(false)
const previewDialogVisible = ref(false)
const previewDialogTitle = ref('图片预览')
const previewImageUrl = ref('')
const documentMeta = reactive({
  status: 'draft' as HomeContentDocument['status'],
  updatedAt: '',
  publishedAt: '' as string | null,
})

function createEmptyServiceItem(): HomeServiceItem {
  return {
    id: `service-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    iconUrl: '',
    name: '',
    description: '',
    link: '',
  }
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
  },
  servicesSection: {
    enabled: true,
    title: '',
    description: '',
    items: [],
  },
  contact: {
    title: '',
    description: '',
    phone: '',
    email: '',
  },
  seo: {
    title: '',
    description: '',
    keywords: '',
  },
})

const initialSnapshot = ref<HomeContentForm | null>(null)
const rules: FormRules<HomeContentForm> = {
  'hero.title': [{ required: true, message: '请输入主标题', trigger: 'blur' }],
  'hero.subtitle': [{ required: true, message: '请输入次要内容', trigger: 'blur' }],
  'hero.primaryButtonText': [{ required: true, message: '请输入按钮一名称', trigger: 'blur' }],
  'hero.primaryButtonLink': [{ required: true, message: '请输入按钮一链接', trigger: 'blur' }],
  'hero.secondaryButtonText': [{ required: true, message: '请输入按钮二名称', trigger: 'blur' }],
  'hero.secondaryButtonLink': [{ required: true, message: '请输入按钮二链接', trigger: 'blur' }],
  'tracking.title': [{ required: true, message: '请输入运单追踪模块标题', trigger: 'blur' }],
  'servicesSection.title': [{ required: true, message: '请输入主营业务模块标题', trigger: 'blur' }],
  'servicesSection.description': [{ required: true, message: '请输入主营业务模块描述', trigger: 'blur' }],
  'contact.title': [{ required: true, message: '请输入联系模块标题', trigger: 'blur' }],
  'contact.phone': [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  'contact.email': [{ required: true, message: '请输入联系邮箱', trigger: 'blur' }],
  'seo.title': [{ required: true, message: '请输入 SEO 标题', trigger: 'blur' }],
  'seo.description': [{ required: true, message: '请输入 SEO 描述', trigger: 'blur' }],
}

const statusLabel = computed(() => (documentMeta.status === 'published' ? '已发布' : '草稿'))
const statusTagType = computed(() => (documentMeta.status === 'published' ? 'success' : 'info'))
const primaryBannerImage = computed(() => form.hero.images[0] ?? null)
const previewBackgroundStyle = computed(() => {
  if (!primaryBannerImage.value?.url) {
    return 'linear-gradient(rgba(15,23,42,0.72), rgba(15,23,42,0.78))'
  }

  return `linear-gradient(rgba(15,23,42,0.45), rgba(15,23,42,0.65)), url(${primaryBannerImage.value.url})`
})
const previewServiceItems = computed(() => form.servicesSection.items.slice(0, 6))

function cloneForm(source: HomeContentForm): HomeContentForm {
  return JSON.parse(JSON.stringify(source)) as HomeContentForm
}

function normaliseServiceItems(items: HomeServiceItem[]) {
  return items.length > 0
    ? items.map((item) => ({
        id: item.id || createEmptyServiceItem().id,
        iconUrl: item.iconUrl ?? '',
        name: item.name ?? '',
        description: item.description ?? '',
        link: item.link ?? '',
      }))
    : [createEmptyServiceItem()]
}

function applyForm(source: HomeContentForm) {
  const snapshot = cloneForm(source)
  form.hero = snapshot.hero
  form.tracking = snapshot.tracking
  form.servicesSection = {
    enabled: snapshot.servicesSection.enabled,
    title: snapshot.servicesSection.title,
    description: snapshot.servicesSection.description,
    items: normaliseServiceItems(snapshot.servicesSection.items),
  }
  form.contact = snapshot.contact
  form.seo = snapshot.seo
}

function applyDocument(document: HomeContentDocument) {
  documentMeta.status = document.status
  documentMeta.updatedAt = document.updatedAt
  documentMeta.publishedAt = document.publishedAt
  applyForm(document.form)
  initialSnapshot.value = cloneForm({
    ...document.form,
    servicesSection: {
      ...document.form.servicesSection,
      items: normaliseServiceItems(document.form.servicesSection.items),
    },
  })
}

function createBannerId() {
  return `banner-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function readFileAsDataUrl(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}

async function loadContent() {
  loading.value = true
  try {
    applyDocument(await fetchHomeContent())
  } finally {
    loading.value = false
  }
}

async function validateForm() {
  if (!formRef.value) return false
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return false
  if (form.hero.images.length === 0) return ElMessage.error('请至少上传一张 Banner 图片'), false
  if (form.servicesSection.enabled && form.servicesSection.items.length === 0) {
    return ElMessage.error('请至少添加一个主营业务模块'), false
  }
  if (
    form.servicesSection.enabled &&
    form.servicesSection.items.some((item) => !item.name.trim() || !item.description.trim() || !item.link.trim())
  ) {
    ElMessage.error('请完善所有主营业务模块的名称、描述和查看更多路由')
    return false
  }
  if (form.tracking.enabled && !form.tracking.title.trim()) {
    return ElMessage.error('请填写运单追踪模块标题'), false
  }
  return true
}

async function onBannerFileChange(uploadFile: UploadFile, uploadFiles: UploadFiles) {
  const raw = uploadFile.raw
  if (!raw) return
  if (!raw.type.startsWith('image/')) return ElMessage.error('只能上传图片文件')
  if (raw.size > 3 * 1024 * 1024) return ElMessage.error('单张图片请控制在 3MB 以内')
  const image: HomeBannerImage = {
    id: createBannerId(),
    name: raw.name,
    url: await readFileAsDataUrl(raw),
  }
  form.hero.images.push(image)
  uploadFiles.splice(0, uploadFiles.length)
  ElMessage.success(`已添加图片：${raw.name}`)
}

function onRemoveBanner(index: number) {
  const [removed] = form.hero.images.splice(index, 1)
  if (removed) ElMessage.success(`已删除图片：${removed.name}`)
}

function onSetAsPrimary(index: number) {
  const [selected] = form.hero.images.splice(index, 1)
  if (!selected) return
  form.hero.images.unshift(selected)
  ElMessage.success('已设为首图')
}

async function onServiceIconChange(index: number, uploadFile: UploadFile, uploadFiles: UploadFiles) {
  const raw = uploadFile.raw
  if (!raw) return
  if (!raw.type.startsWith('image/')) return ElMessage.error('图标只能上传图片文件')
  if (raw.size > 1024 * 1024) return ElMessage.error('图标请控制在 1MB 以内')
  form.servicesSection.items[index].iconUrl = await readFileAsDataUrl(raw)
  uploadFiles.splice(0, uploadFiles.length)
  ElMessage.success(`已更新业务图标：${raw.name}`)
}

function createServiceIconUploader(index: number) {
  return (uploadFile: UploadFile, uploadFiles: UploadFiles) => onServiceIconChange(index, uploadFile, uploadFiles)
}

function onRemoveServiceIcon(index: number) {
  form.servicesSection.items[index].iconUrl = ''
  ElMessage.success('已移除业务图标')
}

function onAddService() {
  form.servicesSection.items.push(createEmptyServiceItem())
  ElMessage.success('已新增业务模块')
}

function onRemoveService(index: number) {
  form.servicesSection.items.splice(index, 1)
  ElMessage.success('已删除业务模块')
}

function openPreview(title: string, url: string) {
  previewDialogTitle.value = title
  previewImageUrl.value = url
  previewDialogVisible.value = true
}

async function onSaveDraft() {
  if (!(await validateForm())) return
  saving.value = true
  try {
    applyDocument(await saveHomeContentDraft(cloneForm(form)))
    ElMessage.success('首页内容草稿已保存')
  } finally {
    saving.value = false
  }
}

async function onPublish() {
  if (!(await validateForm())) return
  publishing.value = true
  try {
    applyDocument(await publishHomeContent(cloneForm(form)))
    ElMessage.success('首页内容已发布')
  } finally {
    publishing.value = false
  }
}

function onReset() {
  if (!initialSnapshot.value) return
  applyForm(initialSnapshot.value)
  formRef.value?.clearValidate()
  ElMessage.info('已恢复到最近一次保存内容')
}

onMounted(loadContent)
</script>
