<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">支付管理</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          在一个页面里维护支付商户、支付订单、退款、回调重放和对账记录。
        </p>
      </div>
      <div class="flex flex-wrap gap-3">
        <el-button :loading="loading" @click="loadData">刷新</el-button>
        <el-button v-if="canManageMerchant" @click="openMerchantDialog()">新建商户</el-button>
        <el-button v-if="canEdit" @click="openReconcileDialog">新建对账</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">新建支付单</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <template #header>
        <div>
          <h3 class="m-0 text-base font-bold text-ink">当前商户</h3>
          <p class="m-0 mt-1 text-sm text-mist">同一时间只有一个生效商户，新支付单默认使用当前生效商户。</p>
        </div>
      </template>

      <div class="grid gap-4 lg:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)]">
        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div v-if="activeMerchant" class="grid gap-3 text-sm text-ink md:grid-cols-2">
            <div><strong>商户名称：</strong>{{ activeMerchant.merchantName }}</div>
            <div><strong>商户编码：</strong>{{ activeMerchant.merchantCode }}</div>
            <div><strong>Mch ID：</strong>{{ activeMerchant.mchId }}</div>
            <div><strong>App ID：</strong>{{ activeMerchant.appId }}</div>
            <div class="md:col-span-2"><strong>通知地址：</strong>{{ activeMerchant.notifyUrl }}</div>
            <div><strong>状态：</strong><el-tag type="success">已启用</el-tag></div>
            <div>
              <strong>配置完整度：</strong>
              <el-tag :type="activeMerchant.configurationReady ? 'success' : 'warning'">
                {{ activeMerchant.configurationReady ? '已完整' : '待补充' }}
              </el-tag>
            </div>
            <div v-if="activeMerchant.configurationIssues.length" class="md:col-span-2 text-red-500">
              <strong>缺失项：</strong>{{ activeMerchant.configurationIssues.join('、') }}
            </div>
            <div><strong>更新时间：</strong>{{ activeMerchant.updatedAt }}</div>
          </div>
          <el-empty v-else description="暂无生效商户" />
        </div>

        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">商户列表</div>
          <div class="space-y-3">
            <div
              v-for="item in merchantConfigs"
              :key="item.id"
              class="flex flex-col gap-3 rounded-2xl border border-line px-4 py-3 md:flex-row md:items-center md:justify-between"
            >
              <div class="min-w-0">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-ink">{{ item.merchantName }}</span>
                  <el-tag v-if="item.active" size="small" type="success">当前生效</el-tag>
                  <el-tag v-else-if="!item.enabled" size="small" type="info">已禁用</el-tag>
                </div>
                <div class="mt-1 text-sm text-mist">{{ item.mchId }} / {{ item.appId }}</div>
                <div class="mt-2 flex flex-wrap gap-2 text-xs">
                  <el-tag size="small" :type="item.configurationReady ? 'success' : 'warning'">
                    {{ item.configurationReady ? '配置完整' : '配置未完成' }}
                  </el-tag>
                  <el-tag size="small" :type="item.appSecretConfigured ? 'success' : 'danger'">AppSecret</el-tag>
                  <el-tag size="small" :type="item.apiV3KeyConfigured ? 'success' : 'danger'">APIv3Key</el-tag>
                  <el-tag size="small" :type="item.privateKeyConfigured ? 'success' : 'danger'">私钥</el-tag>
                  <el-tag size="small" :type="item.merchantSerialNoConfigured ? 'success' : 'danger'">证书序列号</el-tag>
                  <el-tag size="small" :type="item.platformCertificateConfigured ? 'success' : 'danger'">平台证书</el-tag>
                </div>
                <div v-if="item.configurationIssues.length" class="mt-2 text-xs text-red-500">
                  缺失项：{{ item.configurationIssues.join('、') }}
                </div>
              </div>
              <div class="flex flex-wrap gap-2">
                <el-button v-if="canManageMerchant" size="small" @click="openMerchantDialog(item)">编辑</el-button>
                <el-button
                  v-if="canManageMerchant && item.enabled && !item.active"
                  size="small"
                  type="primary"
                  plain
                  @click="onActivateMerchant(item.id)"
                >
                  设为当前商户
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <template #header>
        <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h3 class="m-0 text-base font-bold text-ink">支付运维</h3>
            <p class="m-0 mt-1 text-sm text-mist">查看证书状态、回调失败统计和对账差异，便于联调与排障。</p>
          </div>
          <div class="flex flex-wrap gap-3">
            <el-button v-if="canManageMerchant" :loading="certificateRefreshing" @click="onRefreshCertificate">
              刷新平台证书
            </el-button>
          </div>
        </div>
      </template>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1.05fr)_minmax(0,1fr)_minmax(0,1fr)]">
        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">商户证书状态</div>
          <div v-if="opsOverview?.currentMerchantCertificate" class="space-y-2 text-sm text-ink">
            <div><strong>商户：</strong>{{ opsOverview.currentMerchantCertificate.merchantName }}</div>
            <div><strong>Mch ID：</strong>{{ opsOverview.currentMerchantCertificate.mchId }}</div>
            <div><strong>自动刷新：</strong>{{ opsOverview.currentMerchantCertificate.autoRefreshEnabled ? '已开启' : '未开启' }}</div>
            <div><strong>最近更新：</strong>{{ opsOverview.currentMerchantCertificate.lastUpdatedAt || '-' }}</div>
            <div class="break-all"><strong>证书路径：</strong>{{ opsOverview.currentMerchantCertificate.certificatePath || '-' }}</div>
          </div>
          <el-empty v-else description="暂无证书状态" />
        </div>

        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">支付回调失败</div>
          <div v-if="opsOverview?.paymentNotifyFailures.length" class="space-y-3">
            <div
              v-for="item in opsOverview.paymentNotifyFailures"
              :key="`payment-${item.category}`"
              class="rounded-2xl border border-line px-3 py-3 text-sm"
            >
              <div class="font-semibold text-ink">{{ item.category }}</div>
              <div class="mt-1 text-mist">次数：{{ item.count }}</div>
              <div class="mt-1 text-mist">最近时间：{{ item.latestCreatedAt || '-' }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无支付回调失败记录" />
        </div>

        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">退款回调失败</div>
          <div v-if="opsOverview?.refundNotifyFailures.length" class="space-y-3">
            <div
              v-for="item in opsOverview.refundNotifyFailures"
              :key="`refund-${item.category}`"
              class="rounded-2xl border border-line px-3 py-3 text-sm"
            >
              <div class="font-semibold text-ink">{{ item.category }}</div>
              <div class="mt-1 text-mist">次数：{{ item.count }}</div>
              <div class="mt-1 text-mist">最近时间：{{ item.latestCreatedAt || '-' }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无退款回调失败记录" />
        </div>
      </div>
    </el-card>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="mb-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_220px_220px]">
        <el-input v-model="keyword" clearable placeholder="按支付单号、交易流水号、手机号或运单号搜索" />
        <el-select v-model="statusFilter" clearable placeholder="按状态筛选">
          <el-option label="全部状态" value="" />
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="channelFilter" clearable placeholder="按渠道筛选">
          <el-option label="全部渠道" value="" />
          <el-option v-for="item in channelOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-table :data="payments" v-loading="loading" border class="overflow-hidden rounded-2xl">
        <el-table-column prop="orderNo" label="支付单号" min-width="180" />
        <el-table-column prop="merchantName" label="商户" min-width="160" />
        <el-table-column prop="memberPhone" label="会员" min-width="140" />
        <el-table-column prop="waybillTrackingNo" label="关联运单" min-width="160" />
        <el-table-column label="金额" width="160">
          <template #default="{ row }">
            {{ row.currency }} {{ row.amountTotal.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="支付渠道" width="130" />
        <el-table-column prop="externalTransactionNo" label="交易流水号" min-width="180" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" @click="openDetailDialog(row.id)">详情</el-button>
              <el-button v-if="canEdit && row.status === 'paid'" size="small" type="warning" plain @click="openRefundDialog(row.id)">
                退款
              </el-button>
              <el-button
                v-if="canEdit && row.status !== 'paid'"
                size="small"
                type="success"
                plain
                @click="onChangeStatus(row.id, 'paid')"
              >
                标记已支付
              </el-button>
              <el-button
                v-if="canEdit && row.status !== 'closed'"
                size="small"
                type="danger"
                plain
                @click="onChangeStatus(row.id, 'closed')"
              >
                关闭
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <template #header>
        <div>
          <h3 class="m-0 text-base font-bold text-ink">对账记录</h3>
          <p class="m-0 mt-1 text-sm text-mist">记录每日对账结果和差异数量，便于回查支付链路。</p>
        </div>
      </template>

      <el-table :data="reconcileRecords" border class="overflow-hidden rounded-2xl">
        <el-table-column prop="reconcileDate" label="对账日期" min-width="140" />
        <el-table-column prop="channel" label="支付渠道" width="140" />
        <el-table-column prop="reconcileStatus" label="状态" width="160" />
        <el-table-column prop="diffCount" label="差异数量" width="120" />
        <el-table-column prop="summary" label="摘要" min-width="220" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openReconcileDiffDialog(row.id)">查看差异</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="merchantDialogVisible"
      :title="merchantEditingId ? '编辑商户' : '新建商户'"
      width="760px"
      destroy-on-close
      @closed="onMerchantDialogClosed"
    >
      <el-form ref="merchantFormRef" :model="merchantForm" :rules="merchantRules" label-position="top" class="space-y-6">
        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="商户名称" prop="merchantName">
            <el-input v-model="merchantForm.merchantName" />
          </el-form-item>
          <el-form-item label="商户编码" prop="merchantCode">
            <el-input v-model="merchantForm.merchantCode" :disabled="merchantEditingId !== null" />
          </el-form-item>
          <el-form-item label="Mch ID" prop="mchId">
            <el-input v-model="merchantForm.mchId" />
          </el-form-item>
          <el-form-item label="App ID" prop="appId">
            <el-input v-model="merchantForm.appId" />
          </el-form-item>
          <el-form-item label="App Secret">
            <el-input v-model="merchantForm.appSecret" show-password />
          </el-form-item>
          <el-form-item label="通知地址" prop="notifyUrl" class="md:col-span-2">
            <el-input v-model="merchantForm.notifyUrl" />
          </el-form-item>
          <el-form-item label="APIv3 Key">
            <el-input v-model="merchantForm.apiV3Key" />
          </el-form-item>
          <el-form-item label="商户证书序列号">
            <el-input v-model="merchantForm.merchantSerialNo" />
          </el-form-item>
          <el-form-item label="商户私钥路径">
            <el-input v-model="merchantForm.privateKeyPath" />
          </el-form-item>
          <el-form-item label="平台证书路径">
            <el-input v-model="merchantForm.platformCertificatePath" />
          </el-form-item>
          <el-form-item label="备注" class="md:col-span-2">
            <el-input v-model="merchantForm.remark" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="是否启用">
            <el-switch v-model="merchantForm.enabled" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="merchantDialogVisible = false">取消</el-button>
          <el-button v-if="canManageMerchant" type="primary" :loading="saving" @click="onSaveMerchant">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="createDialogVisible" title="新建支付单" width="760px" destroy-on-close @closed="onCreateDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-6">
        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="支付商户">
            <el-select v-model="form.merchantConfigId" clearable placeholder="默认使用当前生效商户">
              <el-option
                v-for="item in enabledMerchantOptions"
                :key="item.id"
                :label="`${item.merchantName}${item.active ? ' / 当前生效' : ''}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="会员" prop="memberId">
            <el-select v-model="form.memberId" filterable placeholder="请选择会员">
              <el-option
                v-for="item in memberOptions"
                :key="item.id"
                :label="`${item.phone}${item.fullName ? ` / ${item.fullName}` : ''}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联运单" prop="waybillId">
            <el-select v-model="form.waybillId" filterable clearable placeholder="请选择关联运单">
              <el-option
                v-for="item in waybillOptions"
                :key="item.id"
                :label="`${item.mainTrackingNo} / ${item.customerName}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="业务类型" prop="businessType">
            <el-input v-model="form.businessType" />
          </el-form-item>
          <el-form-item label="场景类型" prop="sceneType">
            <el-input v-model="form.sceneType" />
          </el-form-item>
          <el-form-item label="支付渠道" prop="channel">
            <el-select v-model="form.channel">
              <el-option v-for="item in channelOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="币种" prop="currency">
            <el-input v-model="form.currency" />
          </el-form-item>
          <el-form-item label="支付金额" prop="amountTotal">
            <el-input-number v-model="form.amountTotal" :min="0.01" :precision="2" class="!w-full" />
          </el-form-item>
          <el-form-item label="支付描述" prop="description">
            <el-input v-model="form.description" />
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="md:col-span-2">
            <el-input v-model="form.remark" type="textarea" :rows="3" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button v-if="canEdit" type="primary" :loading="saving" @click="onCreatePayment">创建</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="支付详情" width="980px" destroy-on-close @closed="onDetailDialogClosed">
      <div v-if="detail" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <div class="grid gap-4 text-sm text-ink md:grid-cols-2">
            <div><strong>支付单号：</strong>{{ detail.orderNo }}</div>
            <div><strong>状态：</strong>{{ formatStatus(detail.status) }}</div>
            <div><strong>商户：</strong>{{ detail.merchantName }}</div>
            <div><strong>Mch/App：</strong>{{ detail.merchantMchId }} / {{ detail.merchantAppId }}</div>
            <div><strong>会员：</strong>{{ detail.memberPhone }}<span v-if="detail.memberNickname"> / {{ detail.memberNickname }}</span></div>
            <div><strong>运单：</strong>{{ detail.waybillTrackingNo || '-' }}</div>
            <div><strong>总金额：</strong>{{ detail.currency }} {{ detail.amountTotal.toFixed(2) }}</div>
            <div><strong>实付金额：</strong>{{ detail.currency }} {{ detail.amountPaid.toFixed(2) }}</div>
            <div><strong>支付渠道：</strong>{{ detail.channel }}</div>
            <div><strong>交易流水号：</strong>{{ detail.externalTransactionNo || '-' }}</div>
            <div class="md:col-span-2"><strong>支付描述：</strong>{{ detail.description || '-' }}</div>
            <div class="md:col-span-2"><strong>备注：</strong>{{ detail.remark || '-' }}</div>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header><div class="font-semibold text-slate-900">交易记录</div></template>
          <el-table :data="detail.transactions" border size="small" class="overflow-hidden rounded-2xl">
            <el-table-column prop="transactionType" label="类型" min-width="140" />
            <el-table-column prop="transactionStatus" label="状态" min-width="140" />
            <el-table-column prop="externalTransactionNo" label="交易流水号" min-width="180" />
            <el-table-column prop="externalOutTradeNo" label="外部单号" min-width="180" />
            <el-table-column prop="successTime" label="成功时间" min-width="180" />
          </el-table>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header><div class="font-semibold text-slate-900">支付回调日志</div></template>
          <el-table :data="detail.notifyLogs" border size="small" class="overflow-hidden rounded-2xl">
            <el-table-column prop="notifyType" label="类型" min-width="140" />
            <el-table-column prop="notifyStatus" label="状态" min-width="140" />
            <el-table-column prop="resourceId" label="资源 ID" min-width="160" />
            <el-table-column prop="processResult" label="处理结果" min-width="220" />
            <el-table-column prop="createdAt" label="创建时间" min-width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button size="small" :disabled="!isReplayablePaymentNotify(row.notifyStatus)" @click="onReplayPaymentNotify(row.id)">
                  重放
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header><div class="font-semibold text-slate-900">退款记录</div></template>
          <el-table :data="detail.refunds" border size="small" class="overflow-hidden rounded-2xl">
            <el-table-column prop="refundNo" label="退款单号" min-width="180" />
            <el-table-column prop="amountRefund" label="退款金额" min-width="120" />
            <el-table-column prop="status" label="状态" min-width="140" />
            <el-table-column prop="externalRefundNo" label="退款流水号" min-width="180" />
            <el-table-column prop="refundedAt" label="退款时间" min-width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button size="small" :disabled="row.status !== 'failed'" @click="onRetryRefund(row.id)">
                  重试
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header><div class="font-semibold text-slate-900">退款回调日志</div></template>
          <el-table :data="detail.refundNotifyLogs" border size="small" class="overflow-hidden rounded-2xl">
            <el-table-column prop="notifyType" label="类型" min-width="140" />
            <el-table-column prop="notifyStatus" label="状态" min-width="140" />
            <el-table-column prop="resourceId" label="资源 ID" min-width="160" />
            <el-table-column prop="processResult" label="处理结果" min-width="220" />
            <el-table-column prop="createdAt" label="创建时间" min-width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button size="small" :disabled="!isReplayableRefundNotify(row.notifyStatus)" @click="onReplayRefundNotify(row.id)">
                  重放
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-dialog>

    <el-dialog v-model="refundDialogVisible" title="创建退款" width="520px" destroy-on-close @closed="onRefundDialogClosed">
      <el-form ref="refundFormRef" :model="refundForm" label-position="top">
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amountRefund" :min="0.01" :precision="2" class="!w-full" />
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onCreateRefund">提交退款</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="reconcileDialogVisible" title="新建对账记录" width="560px" destroy-on-close @closed="onReconcileDialogClosed">
      <el-form :model="reconcileForm" label-position="top">
        <el-form-item label="对账日期">
          <el-input v-model="reconcileForm.reconcileDate" placeholder="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="支付渠道">
          <el-select v-model="reconcileForm.channel">
            <el-option v-for="item in channelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="reconcileForm.reconcileStatus" />
        </el-form-item>
        <el-form-item label="差异数量">
          <el-input-number v-model="reconcileForm.diffCount" :min="0" class="!w-full" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="reconcileForm.summary" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="reconcileDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="onCreateReconcile">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="reconcileDiffDialogVisible"
      title="对账差异详情"
      width="760px"
      destroy-on-close
      @closed="onReconcileDiffDialogClosed"
    >
      <div v-if="reconcileDiffDetail" class="space-y-4">
        <div class="grid gap-4 rounded-2xl border border-line bg-white/70 p-4 md:grid-cols-2">
          <div><strong>对账日期：</strong>{{ reconcileDiffDetail.reconcileDate }}</div>
          <div><strong>支付渠道：</strong>{{ reconcileDiffDetail.channel }}</div>
          <div><strong>状态：</strong>{{ reconcileDiffDetail.reconcileStatus }}</div>
          <div><strong>差异数量：</strong>{{ reconcileDiffDetail.diffCount }}</div>
          <div class="md:col-span-2"><strong>摘要：</strong>{{ reconcileDiffDetail.summary || '-' }}</div>
        </div>

        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">差异明细</div>
          <div v-if="reconcileDiffDetail.diffItems.length" class="space-y-2">
            <div
              v-for="(item, index) in reconcileDiffDetail.diffItems"
              :key="`${reconcileDiffDetail.id}-${index}`"
              class="rounded-xl border border-line px-3 py-2 text-sm text-ink"
            >
              {{ item }}
            </div>
          </div>
          <el-empty v-else description="暂无差异明细" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { fetchDictionaryOptions } from '../api/dictionary'
import { fetchMembers } from '../api/member'
import {
  activatePaymentMerchant,
  createEmptyMerchantPayload,
  createEmptyPaymentPayload,
  createEmptyReconcilePayload,
  createEmptyRefundPayload,
  createPayment,
  createPaymentMerchant,
  createReconcileRecord,
  createRefund,
  fetchPayment,
  fetchPaymentMerchants,
  fetchPaymentOpsOverview,
  fetchPayments,
  fetchReconcileDiffDetail,
  fetchReconcileRecords,
  refreshCurrentMerchantCertificate,
  replayPaymentNotifyLog,
  replayRefundNotifyLog,
  retryRefund,
  updatePaymentMerchant,
  updatePaymentStatus,
} from '../api/payment'
import { fetchWaybills } from '../api/waybill'
import { useAuthStore } from '../stores/auth'
import type { DictionaryOption } from '../types/dictionary'
import type { MemberAdminSummary } from '../types/member'
import type {
  MerchantCertificateStatus,
  PayMerchantConfig,
  PayMerchantConfigPayload,
  PaymentAdminDetail,
  PaymentAdminSummary,
  PaymentCreatePayload,
  PaymentOpsOverview,
  ReconcileCreatePayload,
  ReconcileDiffDetail,
  ReconcileRecord,
  RefundCreatePayload,
} from '../types/payment'
import type { WaybillSummary } from '../types/waybill'
import { confirmAction, showErrorMessage, showSuccessMessage } from '../utils/message'
import { hasText, isPositiveAmount } from '../utils/validation'

const auth = useAuthStore()
const canEdit = computed(() => auth.hasPermission('payment:edit'))
const canManageMerchant = computed(() => auth.hasPermission('payment:merchant') || auth.hasPermission('payment:edit'))

const loading = ref(false)
const saving = ref(false)
const certificateRefreshing = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const channelFilter = ref('')
const payments = ref<PaymentAdminSummary[]>([])
const statusOptions = ref<DictionaryOption[]>([])
const channelOptions = ref<DictionaryOption[]>([])
const memberOptions = ref<MemberAdminSummary[]>([])
const waybillOptions = ref<WaybillSummary[]>([])
const reconcileRecords = ref<ReconcileRecord[]>([])
const merchantConfigs = ref<PayMerchantConfig[]>([])
const opsOverview = ref<PaymentOpsOverview | null>(null)
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const refundDialogVisible = ref(false)
const reconcileDialogVisible = ref(false)
const reconcileDiffDialogVisible = ref(false)
const merchantDialogVisible = ref(false)
const detail = ref<PaymentAdminDetail | null>(null)
const reconcileDiffDetail = ref<ReconcileDiffDetail | null>(null)
const selectedPaymentId = ref<number | null>(null)
const merchantEditingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const refundFormRef = ref<FormInstance>()
const merchantFormRef = ref<FormInstance>()

const form = reactive<PaymentCreatePayload>(createEmptyPaymentPayload())
const refundForm = reactive<RefundCreatePayload>(createEmptyRefundPayload())
const reconcileForm = reactive<ReconcileCreatePayload>(createEmptyReconcilePayload())
const merchantForm = reactive<PayMerchantConfigPayload>(createEmptyMerchantPayload())

const rules: FormRules = {
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  channel: [{ required: true, message: '请选择支付渠道', trigger: 'change' }],
  amountTotal: [{ required: true, message: '请输入支付金额', trigger: 'change' }],
}

const merchantRules: FormRules = {
  merchantName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }],
  merchantCode: [{ required: true, message: '请输入商户编码', trigger: 'blur' }],
  mchId: [{ required: true, message: '请输入 Mch ID', trigger: 'blur' }],
  appId: [{ required: true, message: '请输入 App ID', trigger: 'blur' }],
  notifyUrl: [{ required: true, message: '请输入通知地址', trigger: 'blur' }],
}

watch([keyword, statusFilter, channelFilter], () => {
  loadPayments()
})

function toLabelMap(items: DictionaryOption[]) {
  return Object.fromEntries(items.map((item) => [item.value, item.label]))
}

const statusLabelMap = computed(() => toLabelMap(statusOptions.value))
const activeMerchant = computed(() => merchantConfigs.value.find((item) => item.active) ?? null)
const enabledMerchantOptions = computed(() => merchantConfigs.value.filter((item) => item.enabled))

function formatStatus(value: string) {
  return statusLabelMap.value[value] ?? value
}

function statusTagType(status: string) {
  if (status === 'paid' || status === 'refunded') return 'success'
  if (status === 'closed' || status === 'exception') return 'danger'
  if (status === 'paying' || status === 'refunding') return 'warning'
  return 'info'
}

function isReplayablePaymentNotify(status: string) {
  return !['paid', 'closed', 'SUCCESS', 'received'].includes(status)
}

function isReplayableRefundNotify(status: string) {
  return !['succeeded', 'SUCCESS', 'received'].includes(status)
}

function resetForm() {
  Object.assign(form, createEmptyPaymentPayload())
}

function resetRefundForm() {
  Object.assign(refundForm, createEmptyRefundPayload())
}

function resetReconcileForm() {
  Object.assign(reconcileForm, createEmptyReconcilePayload())
}

function resetMerchantForm() {
  Object.assign(merchantForm, createEmptyMerchantPayload())
}

async function loadDictionaryData() {
  const data = await fetchDictionaryOptions(['pay_order_status', 'pay_channel'])
  statusOptions.value = data.pay_order_status ?? []
  channelOptions.value = data.pay_channel ?? []
}

async function loadPayments() {
  loading.value = true
  try {
    payments.value = await fetchPayments({
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      channel: channelFilter.value || undefined,
    })
  } catch (error) {
    showErrorMessage(error, '支付单列表加载失败')
  } finally {
    loading.value = false
  }
}

async function loadReconcileRecords() {
  reconcileRecords.value = await fetchReconcileRecords(channelFilter.value || undefined)
}

async function loadOpsOverview() {
  opsOverview.value = await fetchPaymentOpsOverview()
}

async function loadSelectorData() {
  const [members, waybills] = await Promise.all([fetchMembers(), fetchWaybills()])
  memberOptions.value = members
  waybillOptions.value = waybills
}

async function loadMerchantConfigs() {
  merchantConfigs.value = await fetchPaymentMerchants()
}

async function refreshListAndDetail(paymentId?: number | null) {
  await loadPayments()
  if (paymentId) {
    detail.value = await fetchPayment(paymentId)
  }
}

async function loadData() {
  loading.value = true
  try {
    await Promise.all([
      loadDictionaryData(),
      loadSelectorData(),
      loadPayments(),
      loadReconcileRecords(),
      loadMerchantConfigs(),
      loadOpsOverview(),
    ])
  } catch (error) {
    showErrorMessage(error, '支付管理数据加载失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  resetForm()
  form.merchantConfigId = activeMerchant.value?.id ?? null
  createDialogVisible.value = true
}

function openRefundDialog(id: number) {
  selectedPaymentId.value = id
  resetRefundForm()
  refundDialogVisible.value = true
}

function openReconcileDialog() {
  resetReconcileForm()
  reconcileDialogVisible.value = true
}

function openMerchantDialog(item?: PayMerchantConfig) {
  resetMerchantForm()
  merchantEditingId.value = item?.id ?? null
  if (item) {
    Object.assign(merchantForm, {
      merchantName: item.merchantName,
      merchantCode: item.merchantCode,
      mchId: item.mchId,
      appId: item.appId,
      appSecret: '',
      notifyUrl: item.notifyUrl,
      apiV3Key: '',
      privateKeyPath: '',
      merchantSerialNo: '',
      platformCertificatePath: '',
      enabled: item.enabled,
      remark: item.remark,
    })
  }
  merchantDialogVisible.value = true
}

async function openDetailDialog(id: number) {
  loading.value = true
  try {
    detail.value = await fetchPayment(id)
    detailDialogVisible.value = true
  } catch (error) {
    showErrorMessage(error, '支付详情加载失败')
  } finally {
    loading.value = false
  }
}

async function openReconcileDiffDialog(id: number) {
  loading.value = true
  try {
    reconcileDiffDetail.value = await fetchReconcileDiffDetail(id)
    reconcileDiffDialogVisible.value = true
  } catch (error) {
    showErrorMessage(error, '对账差异详情加载失败')
  } finally {
    loading.value = false
  }
}

async function onSaveMerchant() {
  const valid = merchantFormRef.value
    ? await merchantFormRef.value.validate().then(() => true).catch(() => false)
    : true

  if (!valid) {
    return
  }

  saving.value = true
  try {
    if (merchantEditingId.value) {
      await updatePaymentMerchant(merchantEditingId.value, merchantForm)
      showSuccessMessage('商户更新成功')
    } else {
      await createPaymentMerchant(merchantForm)
      showSuccessMessage('商户创建成功')
    }
    merchantDialogVisible.value = false
    await Promise.all([loadMerchantConfigs(), loadOpsOverview()])
  } catch (error) {
    showErrorMessage(error, '商户保存失败')
  } finally {
    saving.value = false
  }
}

async function onActivateMerchant(id: number) {
  const confirmed = await confirmAction('确认将该商户切换为当前生效商户吗？后续新建支付单会默认使用它。', '切换当前商户')
  if (!confirmed) {
    return
  }

  try {
    await activatePaymentMerchant(id)
    showSuccessMessage('当前商户切换成功')
    await Promise.all([loadMerchantConfigs(), loadOpsOverview()])
  } catch (error) {
    showErrorMessage(error, '当前商户切换失败')
  }
}

async function onRefreshCertificate() {
  certificateRefreshing.value = true
  try {
    const certificateStatus: MerchantCertificateStatus = await refreshCurrentMerchantCertificate()
    if (opsOverview.value) {
      opsOverview.value = {
        ...opsOverview.value,
        currentMerchantCertificate: certificateStatus,
      }
    } else {
      await loadOpsOverview()
    }
    showSuccessMessage('平台证书刷新成功')
  } catch (error) {
    showErrorMessage(error, '平台证书刷新失败')
  } finally {
    certificateRefreshing.value = false
  }
}

async function onReplayPaymentNotify(id: number) {
  if (!detail.value?.id) {
    return
  }

  saving.value = true
  try {
    await replayPaymentNotifyLog(id)
    await Promise.all([refreshListAndDetail(detail.value.id), loadOpsOverview()])
    showSuccessMessage('支付回调重放成功')
  } catch (error) {
    showErrorMessage(error, '支付回调重放失败')
  } finally {
    saving.value = false
  }
}

async function onReplayRefundNotify(id: number) {
  if (!detail.value?.id) {
    return
  }

  saving.value = true
  try {
    await replayRefundNotifyLog(id)
    await Promise.all([refreshListAndDetail(detail.value.id), loadOpsOverview()])
    showSuccessMessage('退款回调重放成功')
  } catch (error) {
    showErrorMessage(error, '退款回调重放失败')
  } finally {
    saving.value = false
  }
}

async function onRetryRefund(id: number) {
  if (!detail.value?.id) {
    return
  }

  saving.value = true
  try {
    await retryRefund(id)
    await Promise.all([refreshListAndDetail(detail.value.id), loadOpsOverview()])
    showSuccessMessage('退款重试成功')
  } catch (error) {
    showErrorMessage(error, '退款重试失败')
  } finally {
    saving.value = false
  }
}

function validateCreatePayment() {
  if (!isPositiveAmount(form.amountTotal)) {
    showErrorMessage('请输入正确的支付金额')
    return false
  }

  if (!hasText(form.businessType) || !hasText(form.sceneType) || !hasText(form.currency)) {
    showErrorMessage('业务类型、场景类型和币种不能为空')
    return false
  }

  return true
}

async function onCreatePayment() {
  const valid = formRef.value
    ? await formRef.value.validate().then(() => true).catch(() => false)
    : true

  if (!valid || !validateCreatePayment()) {
    return
  }

  saving.value = true
  try {
    await createPayment(form)
    showSuccessMessage('支付单创建成功')
    createDialogVisible.value = false
    await loadPayments()
  } catch (error) {
    showErrorMessage(error, '支付单创建失败')
  } finally {
    saving.value = false
  }
}

async function onChangeStatus(id: number, status: string) {
  const confirmed = await confirmAction(
    status === 'paid' ? '确认将该支付单标记为已支付吗？' : '确认关闭这笔支付吗？',
    '更新支付状态',
  )

  if (!confirmed) {
    return
  }

  try {
    await updatePaymentStatus(id, status)
    showSuccessMessage('支付状态更新成功')
    await refreshListAndDetail(detail.value?.id === id ? id : null)
  } catch (error) {
    showErrorMessage(error, '支付状态更新失败')
  }
}

async function onCreateRefund() {
  if (!selectedPaymentId.value || !isPositiveAmount(refundForm.amountRefund)) {
    showErrorMessage('请输入正确的退款金额')
    return
  }

  saving.value = true
  try {
    await createRefund(selectedPaymentId.value, refundForm)
    showSuccessMessage('退款单创建成功')
    refundDialogVisible.value = false
    await refreshListAndDetail(detail.value?.id === selectedPaymentId.value ? selectedPaymentId.value : null)
  } catch (error) {
    showErrorMessage(error, '退款单创建失败')
  } finally {
    saving.value = false
  }
}

async function onCreateReconcile() {
  if (!hasText(reconcileForm.reconcileDate) || !hasText(reconcileForm.reconcileStatus)) {
    showErrorMessage('请填写对账日期和状态')
    return
  }

  saving.value = true
  try {
    await createReconcileRecord(reconcileForm)
    showSuccessMessage('对账记录保存成功')
    reconcileDialogVisible.value = false
    await loadReconcileRecords()
  } catch (error) {
    showErrorMessage(error, '对账记录保存失败')
  } finally {
    saving.value = false
  }
}

function onCreateDialogClosed() {
  resetForm()
  formRef.value?.clearValidate()
}

function onDetailDialogClosed() {
  detail.value = null
}

function onRefundDialogClosed() {
  selectedPaymentId.value = null
  resetRefundForm()
  refundFormRef.value?.clearValidate?.()
}

function onReconcileDialogClosed() {
  resetReconcileForm()
}

function onReconcileDiffDialogClosed() {
  reconcileDiffDetail.value = null
}

function onMerchantDialogClosed() {
  merchantEditingId.value = null
  resetMerchantForm()
  merchantFormRef.value?.clearValidate()
}

onMounted(() => {
  loadData()
})
</script>
