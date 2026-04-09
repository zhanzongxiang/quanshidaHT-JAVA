<template>
  <div class="space-y-6 pb-6">
    <div class="rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel">
      <h2 class="m-0 text-xl font-extrabold text-ink">线路页面</h2>
      <p class="m-0 mt-2 max-w-3xl text-sm leading-6 text-mist">
        这里目前是固定模板线路页的管理入口。当前版本先明确每条线路所使用的模板结构和模块范围，下一步再进入单条线路的模板内容编辑。
      </p>
    </div>

    <el-alert
      title="当前状态"
      type="warning"
      :closable="false"
      class="rounded-3xl"
      description="线路页面现在还不是可编辑表单，而是模板管理入口。要修改模板内容，下一步需要新增‘线路页编辑器’，针对每条线路维护 Hero、优势、服务流程、承运范围和 CTA。"
    />

    <div class="grid gap-6 xl:grid-cols-3">
      <el-card v-for="line in serviceLines" :key="line.code" class="rounded-3xl border-0 shadow-panel">
        <div class="space-y-4">
          <div class="flex items-start justify-between gap-3">
            <div>
              <h3 class="m-0 text-lg font-bold text-ink">{{ line.name }}</h3>
              <p class="m-0 mt-1 text-sm text-mist">路由：{{ line.path }}</p>
            </div>
            <el-tag effect="light" type="warning">固定模板</el-tag>
          </div>

          <p class="m-0 text-sm leading-6 text-slate-600">{{ line.description }}</p>

          <div class="rounded-2xl bg-slate-50 p-4">
            <p class="m-0 text-sm font-semibold text-slate-900">模板模块</p>
            <div class="mt-3 flex flex-wrap gap-2">
              <span
                v-for="section in line.sections"
                :key="section"
                class="rounded-full bg-white px-3 py-1 text-xs font-medium text-slate-600"
              >
                {{ section }}
              </span>
            </div>
          </div>

          <div class="rounded-2xl border border-dashed border-slate-300 p-4">
            <p class="m-0 text-sm font-semibold text-slate-900">后续编辑方式</p>
            <p class="m-0 mt-2 text-sm leading-6 text-mist">
              进入单条线路编辑页后，应分别填写首屏图文、优势 4 项、流程步骤、承运范围列表和底部 CTA。
            </p>
          </div>

          <el-button disabled class="w-full">模板内容编辑器待接入</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
const serviceLines = [
  {
    code: 'taiwan',
    name: '台湾专线',
    path: '/services/taiwan',
    description: '展示台湾线路的首屏横幅、优势说明、服务流程、承运范围和联系引导。',
    sections: ['Hero 首屏', '我们的优势', '服务流程', '承运范围', '底部 CTA'],
  },
  {
    code: 'africa',
    name: '非洲专线',
    path: '/services/africa',
    description: '使用同一固定模板维护非洲线路页面，保证结构稳定、运营内容可控。',
    sections: ['Hero 首屏', '我们的优势', '服务流程', '承运范围', '底部 CTA'],
  },
  {
    code: 'express',
    name: '国际快递',
    path: '/services/express',
    description: '使用统一模板管理国际快递专题页，后续也可扩展更多线路页面。',
    sections: ['Hero 首屏', '我们的优势', '服务流程', '承运范围', '底部 CTA'],
  },
]
</script>
