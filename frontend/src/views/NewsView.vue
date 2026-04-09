<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">新闻管理</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          当前新闻编辑采用区块化表单。正文可按顺序维护段落、小标题、图片和图片说明，更适合新闻详情页版式。
        </p>
      </div>
      <div class="flex gap-3">
        <el-button :loading="loading" @click="loadArticles">刷新</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">新增新闻</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="mb-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_220px]">
        <el-input v-model="keyword" placeholder="按标题或摘要筛选新闻" clearable />
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
          <el-option label="全部状态" value="" />
          <el-option label="草稿" value="draft" />
          <el-option label="已发布" value="published" />
        </el-select>
      </div>

      <el-table :data="filteredArticles" v-loading="loading" border class="overflow-hidden rounded-2xl">
        <el-table-column label="封面" width="110">
          <template #default="{ row }">
            <div class="flex h-16 w-20 items-center justify-center overflow-hidden rounded-xl bg-slate-100">
              <img v-if="row.coverImageUrl" :src="row.coverImageUrl" alt="cover" class="h-full w-full object-cover" />
              <span v-else class="text-xs text-mist">无封面</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="summary" label="摘要" min-width="260" show-overflow-tooltip />
        <el-table-column label="区块数" width="90">
          <template #default="{ row }">
            {{ row.blocks.length }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'warning'" effect="light">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="publishedAt" label="发布时间" width="180" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" @click="openEditDialog(row.id)">编辑</el-button>
              <el-button
                v-if="canEdit && row.status !== 'published'"
                size="small"
                type="success"
                plain
                :loading="publishingId === row.id"
                @click="onPublish(row.id)"
              >
                发布
              </el-button>
              <el-button v-if="canEdit" size="small" type="danger" plain :loading="deletingId === row.id" @click="onDelete(row.id)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑新闻' : '新增新闻'"
      width="980px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div>
              <h3 class="m-0 text-base font-bold text-ink">基础信息</h3>
              <p class="m-0 mt-1 text-sm text-mist">维护新闻标题、摘要、封面和作者。</p>
            </div>
          </template>

          <div class="grid gap-4 md:grid-cols-2">
            <el-form-item label="新闻标题" prop="title" class="md:col-span-2">
              <el-input v-model="form.title" maxlength="120" show-word-limit />
            </el-form-item>
            <el-form-item label="摘要" prop="summary" class="md:col-span-2">
              <el-input v-model="form.summary" type="textarea" :rows="3" maxlength="500" show-word-limit />
            </el-form-item>
            <el-form-item label="封面图链接" prop="coverImageUrl" class="md:col-span-2">
              <el-input v-model="form.coverImageUrl" placeholder="https://example.com/cover.jpg" />
            </el-form-item>
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" maxlength="64" show-word-limit />
            </el-form-item>
            <div class="rounded-2xl border border-slate-200 bg-slate-50 p-3">
              <p class="m-0 text-sm font-semibold text-slate-900">封面预览</p>
              <div class="mt-3 flex h-32 items-center justify-center overflow-hidden rounded-2xl bg-white">
                <img v-if="form.coverImageUrl" :src="form.coverImageUrl" alt="cover preview" class="h-full w-full object-cover" />
                <span v-else class="text-sm text-mist">未填写封面链接</span>
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header>
            <div class="flex items-center justify-between gap-3">
              <div>
                <h3 class="m-0 text-base font-bold text-ink">正文区块</h3>
                <p class="m-0 mt-1 text-sm text-mist">支持段落、小标题、图片和图片说明，按顺序组成新闻详情页。</p>
              </div>
              <div class="flex flex-wrap gap-2">
                <el-dropdown trigger="click">
                  <el-button type="primary">新增区块</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="onAddBlock('paragraph')">段落</el-dropdown-item>
                      <el-dropdown-item @click="onAddBlock('heading')">小标题</el-dropdown-item>
                      <el-dropdown-item @click="onAddBlock('image')">图片</el-dropdown-item>
                      <el-dropdown-item @click="onAddBlock('image_caption')">图片说明</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </template>

          <div class="space-y-4">
            <div
              v-for="(block, index) in form.blocks"
              :key="block.id"
              class="rounded-2xl border border-slate-200 bg-white p-4"
            >
              <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                <div class="flex items-center gap-3">
                  <span class="inline-flex h-8 min-w-8 items-center justify-center rounded-full bg-slate-900 text-sm font-semibold text-white">
                    {{ index + 1 }}
                  </span>
                  <div>
                    <p class="m-0 text-sm font-semibold text-slate-900">{{ blockTypeLabelMap[block.type] }}</p>
                    <p class="m-0 mt-1 text-xs text-mist">根据区块类型填写内容，顺序即前台展示顺序。</p>
                  </div>
                </div>
                <div class="flex flex-wrap gap-2">
                  <el-select v-model="block.type" size="small" class="w-32">
                    <el-option label="段落" value="paragraph" />
                    <el-option label="小标题" value="heading" />
                    <el-option label="图片" value="image" />
                    <el-option label="图片说明" value="image_caption" />
                  </el-select>
                  <el-button size="small" :disabled="index === 0" @click="onMoveBlock(index, -1)">上移</el-button>
                  <el-button size="small" :disabled="index === form.blocks.length - 1" @click="onMoveBlock(index, 1)">下移</el-button>
                  <el-button size="small" type="danger" plain @click="onRemoveBlock(index)">删除</el-button>
                </div>
              </div>

              <div class="grid gap-4 md:grid-cols-[minmax(0,1fr)_280px]">
                <el-form-item :label="blockContentLabel(block.type)" :prop="`blocks.${index}.content`">
                  <el-input
                    v-if="block.type !== 'image'"
                    v-model="block.content"
                    :type="block.type === 'heading' ? 'text' : 'textarea'"
                    :rows="block.type === 'paragraph' ? 6 : 3"
                    :maxlength="block.type === 'heading' ? 120 : 1000"
                    show-word-limit
                  />
                  <el-input v-else v-model="block.content" placeholder="https://example.com/news-image.jpg" />
                </el-form-item>

                <div class="rounded-2xl border border-slate-200 bg-slate-50 p-3">
                  <p class="m-0 text-sm font-semibold text-slate-900">区块预览</p>
                  <div class="mt-3 rounded-2xl bg-white p-4">
                    <p v-if="block.type === 'heading'" class="m-0 text-xl font-extrabold text-ink">
                      {{ block.content || '请输入小标题' }}
                    </p>
                    <p v-else-if="block.type === 'paragraph'" class="m-0 whitespace-pre-wrap text-sm leading-7 text-slate-700">
                      {{ block.content || '请输入段落内容' }}
                    </p>
                    <div v-else-if="block.type === 'image'" class="flex h-36 items-center justify-center overflow-hidden rounded-2xl bg-slate-100">
                      <img v-if="block.content" :src="block.content" alt="news block" class="h-full w-full object-cover" />
                      <span v-else class="text-sm text-mist">未填写图片链接</span>
                    </div>
                    <p v-else class="m-0 text-center text-sm text-mist">
                      {{ block.content || '请输入图片说明' }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <el-empty v-if="form.blocks.length === 0" description="请至少添加一个正文区块" />
          </div>
        </el-card>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createEmptyNewsBlock,
  createNewsArticle,
  deleteNewsArticle,
  fetchNewsArticle,
  fetchNewsArticles,
  publishNewsArticle,
  updateNewsArticle,
} from '../api/news'
import { useAuthStore } from '../stores/auth'
import type { NewsArticle, NewsArticleSavePayload, NewsBlock, NewsBlockType } from '../types/news'

const blockTypeLabelMap: Record<NewsBlockType, string> = {
  paragraph: '段落',
  heading: '小标题',
  image: '图片',
  image_caption: '图片说明',
}

const auth = useAuthStore()
const canEdit = computed(() => Boolean(auth.me))

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const publishingId = ref<number | null>(null)
const deletingId = ref<number | null>(null)
const keyword = ref('')
const statusFilter = ref<'draft' | 'published' | ''>('')
const articles = ref<NewsArticle[]>([])
const formRef = ref<FormInstance>()

const form = reactive<NewsArticleSavePayload>({
  title: '',
  summary: '',
  coverImageUrl: '',
  blocks: [createEmptyNewsBlock('paragraph')],
  author: 'QSD Admin',
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入新闻标题', trigger: 'blur' }],
  summary: [{ required: true, message: '请输入新闻摘要', trigger: 'blur' }],
}

const filteredArticles = computed(() => {
  return articles.value.filter((article) => {
    const matchedKeyword = !keyword.value.trim()
      || article.title.toLowerCase().includes(keyword.value.trim().toLowerCase())
      || article.summary.toLowerCase().includes(keyword.value.trim().toLowerCase())
    const matchedStatus = !statusFilter.value || article.status === statusFilter.value
    return matchedKeyword && matchedStatus
  })
})

function blockContentLabel(type: NewsBlockType) {
  if (type === 'image') {
    return '图片链接'
  }
  if (type === 'heading') {
    return '小标题内容'
  }
  if (type === 'image_caption') {
    return '图片说明'
  }
  return '段落内容'
}

function cloneBlocks(blocks: NewsBlock[]): NewsBlock[] {
  return blocks.map((block) => ({ ...block }))
}

function resetForm() {
  Object.assign(form, {
    title: '',
    summary: '',
    coverImageUrl: '',
    blocks: [createEmptyNewsBlock('paragraph')],
    author: 'QSD Admin',
  })
}

async function loadArticles() {
  loading.value = true
  try {
    articles.value = await fetchNewsArticles()
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(id: number) {
  loading.value = true
  try {
    const article = await fetchNewsArticle(id)
    editingId.value = id
    Object.assign(form, {
      title: article.title,
      summary: article.summary,
      coverImageUrl: article.coverImageUrl,
      blocks: cloneBlocks(article.blocks),
      author: article.author,
    })
    dialogVisible.value = true
  } finally {
    loading.value = false
  }
}

function onAddBlock(type: NewsBlockType) {
  form.blocks.push(createEmptyNewsBlock(type))
}

function onRemoveBlock(index: number) {
  form.blocks.splice(index, 1)
  if (form.blocks.length === 0) {
    form.blocks.push(createEmptyNewsBlock('paragraph'))
  }
}

function onMoveBlock(index: number, direction: -1 | 1) {
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= form.blocks.length) {
    return
  }

  const [current] = form.blocks.splice(index, 1)
  form.blocks.splice(targetIndex, 0, current)
}

function validateBlocks() {
  if (form.blocks.length === 0) {
    ElMessage.error('请至少添加一个正文区块')
    return false
  }

  const invalidBlock = form.blocks.some((block) => !block.content.trim())
  if (invalidBlock) {
    ElMessage.error('请完整填写所有正文区块内容')
    return false
  }

  return true
}

async function onSave() {
  const valid = formRef.value
    ? await formRef.value.validate().then(() => true).catch(() => false)
    : true

  if (!valid || !validateBlocks()) {
    return
  }

  saving.value = true
  try {
    if (editingId.value) {
      await updateNewsArticle(editingId.value, {
        ...form,
        blocks: cloneBlocks(form.blocks),
      })
      ElMessage.success('新闻已更新')
    } else {
      await createNewsArticle({
        ...form,
        blocks: cloneBlocks(form.blocks),
      })
      ElMessage.success('新闻已创建')
    }
    dialogVisible.value = false
    await loadArticles()
  } catch {
    ElMessage.error('保存新闻失败')
  } finally {
    saving.value = false
  }
}

async function onPublish(id: number) {
  publishingId.value = id
  try {
    await publishNewsArticle(id)
    ElMessage.success('新闻已发布')
    await loadArticles()
  } catch {
    ElMessage.error('发布新闻失败')
  } finally {
    publishingId.value = null
  }
}

async function onDelete(id: number) {
  try {
    await ElMessageBox.confirm('删除后不可恢复，确认删除这条新闻吗？', '删除新闻', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  deletingId.value = id
  try {
    await deleteNewsArticle(id)
    ElMessage.success('新闻已删除')
    await loadArticles()
  } catch {
    ElMessage.error('删除新闻失败')
  } finally {
    deletingId.value = null
  }
}

function onDialogClosed() {
  editingId.value = null
  resetForm()
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadArticles()
})
</script>
