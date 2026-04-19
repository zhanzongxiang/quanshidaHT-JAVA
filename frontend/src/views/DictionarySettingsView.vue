<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <h2 class="m-0 text-xl font-extrabold text-ink">字典管理</h2>
          <p class="m-0 mt-2 max-w-3xl text-sm leading-6 text-mist">
            统一维护运单状态、线路类型、分段状态等基础字典。业务模块只保存稳定编码，显示文案统一从字典读取。
          </p>
        </div>
        <div class="flex gap-3">
          <el-button :loading="loading" @click="loadGroups">刷新</el-button>
          <el-button v-if="canEdit" type="primary" @click="openCreateDialog()">新增字典项</el-button>
        </div>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <el-empty v-if="groups.length === 0" description="暂无字典数据" />

      <el-tabs v-else v-model="activeType" class="dictionary-tabs">
        <el-tab-pane
          v-for="group in groups"
          :key="group.dictType"
          :label="`${group.dictName}（${group.items.length}）`"
          :name="group.dictType"
        >
          <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div>
              <h3 class="m-0 text-base font-bold text-ink">{{ group.dictName }}</h3>
              <p class="m-0 mt-1 text-sm text-mist">字典类型：{{ group.dictType }}</p>
            </div>
            <el-button v-if="canEdit" type="primary" plain @click="openCreateDialog(group)">在当前字典中新增</el-button>
          </div>

          <el-table :data="group.items" border class="overflow-hidden rounded-2xl">
            <el-table-column prop="itemLabel" label="字典标签" min-width="150" />
            <el-table-column prop="itemValue" label="字典值" min-width="150" />
            <el-table-column prop="sortNo" label="排序" width="90" />
            <el-table-column label="启用" width="90">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="内置" width="90">
              <template #default="{ row }">
                <el-tag :type="row.builtin ? 'warning' : 'info'">{{ row.builtin ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="180" />
            <el-table-column prop="updatedAt" label="更新时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <div class="flex flex-wrap gap-2">
                  <el-button v-if="canEdit" size="small" @click="openEditDialog(row)">编辑</el-button>
                  <el-button
                    v-if="canEdit && !row.builtin"
                    size="small"
                    type="danger"
                    plain
                    @click="onDelete(row)"
                  >
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑字典项' : '新增字典项'"
      width="640px"
      destroy-on-close
      @closed="onDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="字典名称" prop="dictName">
            <el-input v-model="form.dictName" :disabled="editingBuiltin" />
          </el-form-item>
          <el-form-item label="字典类型" prop="dictType">
            <el-input v-model="form.dictType" :disabled="editingBuiltin" />
          </el-form-item>
          <el-form-item label="字典标签" prop="itemLabel">
            <el-input v-model="form.itemLabel" />
          </el-form-item>
          <el-form-item label="字典值" prop="itemValue">
            <el-input v-model="form.itemValue" :disabled="editingBuiltin" />
          </el-form-item>
          <el-form-item label="排序号" prop="sortNo">
            <el-input-number v-model="form.sortNo" :min="0" class="!w-full" />
          </el-form-item>
          <el-form-item label="是否启用" prop="enabled">
            <el-switch v-model="form.enabled" />
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="md:col-span-2">
            <el-input v-model="form.remark" type="textarea" :rows="3" />
          </el-form-item>
        </div>
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import {
  createDictionaryItem,
  deleteDictionaryItem,
  fetchDictionaryGroups,
  updateDictionaryItem,
} from '../api/dictionary'
import { useAuthStore } from '../stores/auth'
import type { DictionaryGroup, DictionaryItem, DictionaryItemSavePayload } from '../types/dictionary'

const auth = useAuthStore()
const canEdit = computed(() => auth.hasPermission('dict:edit'))

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const editingBuiltin = ref(false)
const activeType = ref('')
const groups = ref<DictionaryGroup[]>([])
const formRef = ref<FormInstance>()

const form = reactive<DictionaryItemSavePayload>({
  dictType: '',
  dictName: '',
  itemLabel: '',
  itemValue: '',
  sortNo: 10,
  enabled: true,
  remark: '',
})

const rules: FormRules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  itemLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  itemValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
}

watch(groups, (nextGroups) => {
  if (!nextGroups.length) {
    activeType.value = ''
    return
  }
  if (!nextGroups.some((group) => group.dictType === activeType.value)) {
    activeType.value = nextGroups[0].dictType
  }
})

function resetForm() {
  Object.assign(form, {
    dictType: '',
    dictName: '',
    itemLabel: '',
    itemValue: '',
    sortNo: 10,
    enabled: true,
    remark: '',
  })
}

async function loadGroups() {
  loading.value = true
  try {
    groups.value = await fetchDictionaryGroups()
  } catch {
    ElMessage.error('字典数据加载失败。')
  } finally {
    loading.value = false
  }
}

function openCreateDialog(group?: DictionaryGroup) {
  editingId.value = null
  editingBuiltin.value = false
  resetForm()
  if (group) {
    form.dictType = group.dictType
    form.dictName = group.dictName
    form.sortNo = group.items.length > 0 ? Math.max(...group.items.map((item) => item.sortNo)) + 10 : 10
  }
  dialogVisible.value = true
}

function openEditDialog(item: DictionaryItem) {
  editingId.value = item.id
  editingBuiltin.value = item.builtin
  Object.assign(form, {
    dictType: item.dictType,
    dictName: item.dictName,
    itemLabel: item.itemLabel,
    itemValue: item.itemValue,
    sortNo: item.sortNo,
    enabled: item.enabled,
    remark: item.remark ?? '',
  })
  dialogVisible.value = true
}

async function onSave() {
  const valid = formRef.value
    ? await formRef.value.validate().then(() => true).catch(() => false)
    : true
  if (!valid) {
    return
  }

  saving.value = true
  try {
    if (editingId.value) {
      await updateDictionaryItem(editingId.value, { ...form })
      ElMessage.success('字典项更新成功。')
    } else {
      await createDictionaryItem({ ...form })
      ElMessage.success('字典项创建成功。')
    }
    dialogVisible.value = false
    await loadGroups()
  } catch {
    ElMessage.error('字典项保存失败。')
  } finally {
    saving.value = false
  }
}

async function onDelete(item: DictionaryItem) {
  try {
    await ElMessageBox.confirm(`确认删除字典项“${item.itemLabel}”吗？`, '删除字典项', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }

  try {
    await deleteDictionaryItem(item.id)
    ElMessage.success('字典项删除成功。')
    await loadGroups()
  } catch {
    ElMessage.error('字典项删除失败。')
  }
}

function onDialogClosed() {
  editingId.value = null
  editingBuiltin.value = false
  resetForm()
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadGroups()
})
</script>
