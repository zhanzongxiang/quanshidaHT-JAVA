<template>
  <div class="space-y-6 pb-6">
    <div class="flex flex-col gap-4 rounded-3xl border border-line bg-panel px-5 py-5 shadow-panel lg:flex-row lg:items-start lg:justify-between">
      <div class="space-y-2">
        <h2 class="m-0 text-xl font-extrabold text-ink">Payments</h2>
        <p class="m-0 max-w-3xl text-sm leading-6 text-mist">
          Manage the current payment merchant, mini-program payment orders, refunds, and reconciliation in one place.
        </p>
      </div>
      <div class="flex flex-wrap gap-3">
        <el-button :loading="loading" @click="loadData">Refresh</el-button>
        <el-button v-if="canManageMerchant" @click="openMerchantDialog()">New Merchant</el-button>
        <el-button v-if="canEdit" @click="openReconcileDialog">New Reconcile</el-button>
        <el-button v-if="canEdit" type="primary" @click="openCreateDialog">New Payment</el-button>
      </div>
    </div>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <template #header>
        <div class="flex items-center justify-between gap-4">
          <div>
            <h3 class="m-0 text-base font-bold text-ink">Current Merchant</h3>
            <p class="m-0 mt-1 text-sm text-mist">Only one merchant is active at a time. New payment orders use the active merchant.</p>
          </div>
        </div>
      </template>

      <div class="grid gap-4 lg:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)]">
        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div v-if="activeMerchant" class="grid gap-3 md:grid-cols-2">
            <div><strong>Merchant:</strong> {{ activeMerchant.merchantName }}</div>
            <div><strong>Code:</strong> {{ activeMerchant.merchantCode }}</div>
            <div><strong>Mch ID:</strong> {{ activeMerchant.mchId }}</div>
            <div><strong>App ID:</strong> {{ activeMerchant.appId }}</div>
            <div class="md:col-span-2"><strong>Notify URL:</strong> {{ activeMerchant.notifyUrl }}</div>
            <div><strong>Status:</strong> <el-tag type="success">Active</el-tag></div>
            <div>
              <strong>Config:</strong>
              <el-tag :type="activeMerchant.configurationReady ? 'success' : 'warning'">
                {{ activeMerchant.configurationReady ? 'Ready' : 'Incomplete' }}
              </el-tag>
            </div>
            <div v-if="activeMerchant.configurationIssues.length" class="md:col-span-2 text-sm text-red-500">
              <strong>Missing:</strong> {{ activeMerchant.configurationIssues.join(', ') }}
            </div>
            <div><strong>Updated:</strong> {{ activeMerchant.updatedAt }}</div>
          </div>
          <el-empty v-else description="No active merchant" />
        </div>

        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">Available Merchants</div>
          <div class="space-y-3">
            <div
              v-for="item in merchantConfigs"
              :key="item.id"
              class="flex flex-col gap-3 rounded-2xl border border-line px-4 py-3 md:flex-row md:items-center md:justify-between"
            >
              <div class="min-w-0">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-ink">{{ item.merchantName }}</span>
                  <el-tag v-if="item.active" size="small" type="success">Current</el-tag>
                  <el-tag v-else-if="!item.enabled" size="small" type="info">Disabled</el-tag>
                </div>
                <div class="mt-1 text-sm text-mist">{{ item.mchId }} / {{ item.appId }}</div>
                <div class="mt-1 flex flex-wrap gap-2 text-xs">
                  <el-tag size="small" :type="item.configurationReady ? 'success' : 'warning'">
                    {{ item.configurationReady ? 'Ready' : 'Incomplete' }}
                  </el-tag>
                  <el-tag size="small" :type="item.appSecretConfigured ? 'success' : 'danger'">AppSecret</el-tag>
                  <el-tag size="small" :type="item.apiV3KeyConfigured ? 'success' : 'danger'">APIv3</el-tag>
                  <el-tag size="small" :type="item.privateKeyConfigured ? 'success' : 'danger'">PrivateKey</el-tag>
                  <el-tag size="small" :type="item.merchantSerialNoConfigured ? 'success' : 'danger'">SerialNo</el-tag>
                  <el-tag size="small" :type="item.platformCertificateConfigured ? 'success' : 'danger'">PlatformCert</el-tag>
                </div>
                <div v-if="item.configurationIssues.length" class="mt-2 text-xs text-red-500">
                  Missing: {{ item.configurationIssues.join(', ') }}
                </div>
              </div>
              <div class="flex flex-wrap gap-2">
                <el-button v-if="canManageMerchant" size="small" @click="openMerchantDialog(item)">Edit</el-button>
                <el-button
                  v-if="canManageMerchant && item.enabled && !item.active"
                  size="small"
                  type="primary"
                  plain
                  @click="onActivateMerchant(item.id)"
                >
                  Activate
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
            <h3 class="m-0 text-base font-bold text-ink">Payment Ops</h3>
            <p class="m-0 mt-1 text-sm text-mist">Track certificate readiness, callback processing failures, and reconciliation diffs.</p>
          </div>
          <div class="flex flex-wrap gap-3">
            <el-button
              v-if="canManageMerchant"
              :loading="certificateRefreshing"
              @click="onRefreshCertificate"
            >
              Refresh Certificate
            </el-button>
          </div>
        </div>
      </template>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1.05fr)_minmax(0,1fr)_minmax(0,1fr)]">
        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">Merchant Certificate</div>
          <div v-if="opsOverview?.currentMerchantCertificate" class="space-y-2 text-sm text-ink">
            <div><strong>Merchant:</strong> {{ opsOverview.currentMerchantCertificate.merchantName }}</div>
            <div><strong>Mch ID:</strong> {{ opsOverview.currentMerchantCertificate.mchId }}</div>
            <div><strong>Auto Refresh:</strong> {{ opsOverview.currentMerchantCertificate.autoRefreshEnabled ? 'Enabled' : 'Disabled' }}</div>
            <div><strong>Last Updated:</strong> {{ opsOverview.currentMerchantCertificate.lastUpdatedAt || '-' }}</div>
            <div class="break-all"><strong>Certificate Path:</strong> {{ opsOverview.currentMerchantCertificate.certificatePath || '-' }}</div>
          </div>
          <el-empty v-else description="No certificate status" />
        </div>

        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">Payment Callback Failures</div>
          <div v-if="opsOverview?.paymentNotifyFailures.length" class="space-y-3">
            <div
              v-for="item in opsOverview.paymentNotifyFailures"
              :key="`payment-${item.category}`"
              class="rounded-2xl border border-line px-3 py-3 text-sm"
            >
              <div class="font-semibold text-ink">{{ item.category }}</div>
              <div class="mt-1 text-mist">Count: {{ item.count }}</div>
              <div class="mt-1 text-mist">Latest: {{ item.latestCreatedAt || '-' }}</div>
            </div>
          </div>
          <el-empty v-else description="No payment callback failures" />
        </div>

        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">Refund Callback Failures</div>
          <div v-if="opsOverview?.refundNotifyFailures.length" class="space-y-3">
            <div
              v-for="item in opsOverview.refundNotifyFailures"
              :key="`refund-${item.category}`"
              class="rounded-2xl border border-line px-3 py-3 text-sm"
            >
              <div class="font-semibold text-ink">{{ item.category }}</div>
              <div class="mt-1 text-mist">Count: {{ item.count }}</div>
              <div class="mt-1 text-mist">Latest: {{ item.latestCreatedAt || '-' }}</div>
            </div>
          </div>
          <el-empty v-else description="No refund callback failures" />
        </div>
      </div>
    </el-card>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <div class="mb-4 grid gap-4 md:grid-cols-[minmax(0,1fr)_220px_220px]">
        <el-input v-model="keyword" clearable placeholder="Search by order no, transaction no, phone, or waybill" />
        <el-select v-model="statusFilter" clearable placeholder="Filter by status">
          <el-option label="All Statuses" value="" />
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="channelFilter" clearable placeholder="Filter by channel">
          <el-option label="All Channels" value="" />
          <el-option v-for="item in channelOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <el-table :data="payments" v-loading="loading" border class="overflow-hidden rounded-2xl">
        <el-table-column prop="orderNo" label="Order No" min-width="180" />
        <el-table-column prop="merchantName" label="Merchant" min-width="160" />
        <el-table-column prop="memberPhone" label="Member" min-width="140" />
        <el-table-column prop="waybillTrackingNo" label="Waybill" min-width="160" />
        <el-table-column label="Amount" width="140">
          <template #default="{ row }">
            {{ row.currency }} {{ row.amountTotal.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="Status" width="140">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="Channel" width="130" />
        <el-table-column prop="externalTransactionNo" label="Transaction No" min-width="180" />
        <el-table-column prop="createdAt" label="Created At" min-width="180" />
        <el-table-column label="Actions" width="250" fixed="right">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-2">
              <el-button size="small" @click="openDetailDialog(row.id)">View</el-button>
              <el-button
                v-if="canEdit && row.status === 'paid'"
                size="small"
                type="warning"
                plain
                @click="openRefundDialog(row.id)"
              >
                Refund
              </el-button>
              <el-button
                v-if="canEdit && row.status !== 'paid'"
                size="small"
                type="success"
                plain
                @click="onChangeStatus(row.id, 'paid')"
              >
                Mark Paid
              </el-button>
              <el-button
                v-if="canEdit && row.status !== 'closed'"
                size="small"
                type="danger"
                plain
                @click="onChangeStatus(row.id, 'closed')"
              >
                Close
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="rounded-3xl border-0 shadow-panel">
      <template #header>
        <div class="flex items-center justify-between">
          <div>
            <h3 class="m-0 text-base font-bold text-ink">Reconcile Records</h3>
            <p class="m-0 mt-1 text-sm text-mist">Track daily reconciliation results and diff counts for payment channels.</p>
          </div>
        </div>
      </template>

      <el-table :data="reconcileRecords" border class="overflow-hidden rounded-2xl">
        <el-table-column prop="reconcileDate" label="Reconcile Date" min-width="140" />
        <el-table-column prop="channel" label="Channel" width="140" />
        <el-table-column prop="reconcileStatus" label="Status" width="160" />
        <el-table-column prop="diffCount" label="Diff Count" width="120" />
        <el-table-column prop="summary" label="Summary" min-width="220" />
        <el-table-column prop="updatedAt" label="Updated At" min-width="180" />
        <el-table-column label="Actions" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openReconcileDiffDialog(row.id)">View Diffs</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="merchantDialogVisible" :title="merchantEditingId ? 'Edit Merchant' : 'New Merchant'" width="760px" destroy-on-close @closed="onMerchantDialogClosed">
      <el-form ref="merchantFormRef" :model="merchantForm" :rules="merchantRules" label-position="top" class="space-y-6">
        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="Merchant Name" prop="merchantName">
            <el-input v-model="merchantForm.merchantName" />
          </el-form-item>
          <el-form-item label="Merchant Code" prop="merchantCode">
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
          <el-form-item label="Notify URL" prop="notifyUrl" class="md:col-span-2">
            <el-input v-model="merchantForm.notifyUrl" />
          </el-form-item>
          <el-form-item label="APIv3 Key">
            <el-input v-model="merchantForm.apiV3Key" />
          </el-form-item>
          <el-form-item label="Merchant Serial No">
            <el-input v-model="merchantForm.merchantSerialNo" />
          </el-form-item>
          <el-form-item label="Private Key Path">
            <el-input v-model="merchantForm.privateKeyPath" />
          </el-form-item>
          <el-form-item label="Platform Certificate Path">
            <el-input v-model="merchantForm.platformCertificatePath" />
          </el-form-item>
          <el-form-item label="Remark" class="md:col-span-2">
            <el-input v-model="merchantForm.remark" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="Enabled">
            <el-switch v-model="merchantForm.enabled" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="merchantDialogVisible = false">Cancel</el-button>
          <el-button v-if="canManageMerchant" type="primary" :loading="saving" @click="onSaveMerchant">Save</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="createDialogVisible" title="New Payment" width="760px" destroy-on-close @closed="onCreateDialogClosed">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-6">
        <div class="grid gap-4 md:grid-cols-2">
          <el-form-item label="Merchant">
            <el-select v-model="form.merchantConfigId" clearable placeholder="Use current active merchant">
              <el-option
                v-for="item in enabledMerchantOptions"
                :key="item.id"
                :label="`${item.merchantName}${item.active ? ' / current' : ''}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Member" prop="memberId">
            <el-select v-model="form.memberId" filterable placeholder="Select member">
              <el-option
                v-for="item in memberOptions"
                :key="item.id"
                :label="`${item.phone}${item.fullName ? ` / ${item.fullName}` : ''}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Waybill" prop="waybillId">
            <el-select v-model="form.waybillId" filterable clearable placeholder="Select related waybill">
              <el-option
                v-for="item in waybillOptions"
                :key="item.id"
                :label="`${item.mainTrackingNo} / ${item.customerName}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Business Type" prop="businessType">
            <el-input v-model="form.businessType" />
          </el-form-item>
          <el-form-item label="Scene Type" prop="sceneType">
            <el-input v-model="form.sceneType" />
          </el-form-item>
          <el-form-item label="Channel" prop="channel">
            <el-select v-model="form.channel">
              <el-option v-for="item in channelOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="Currency" prop="currency">
            <el-input v-model="form.currency" />
          </el-form-item>
          <el-form-item label="Amount" prop="amountTotal">
            <el-input-number v-model="form.amountTotal" :min="0.01" :precision="2" class="!w-full" />
          </el-form-item>
          <el-form-item label="Description" prop="description">
            <el-input v-model="form.description" />
          </el-form-item>
          <el-form-item label="Remark" prop="remark" class="md:col-span-2">
            <el-input v-model="form.remark" type="textarea" :rows="3" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="createDialogVisible = false">Cancel</el-button>
          <el-button v-if="canEdit" type="primary" :loading="saving" @click="onCreatePayment">Create</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="Payment Detail" width="980px" destroy-on-close @closed="onDetailDialogClosed">
      <div v-if="detail" class="space-y-6">
        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <div class="grid gap-4 md:grid-cols-2">
            <div><strong>Order No:</strong> {{ detail.orderNo }}</div>
            <div><strong>Status:</strong> {{ formatStatus(detail.status) }}</div>
            <div><strong>Merchant:</strong> {{ detail.merchantName }}</div>
            <div><strong>Mch/App:</strong> {{ detail.merchantMchId }} / {{ detail.merchantAppId }}</div>
            <div><strong>Member:</strong> {{ detail.memberPhone }}<span v-if="detail.memberNickname"> / {{ detail.memberNickname }}</span></div>
            <div><strong>Waybill:</strong> {{ detail.waybillTrackingNo || '-' }}</div>
            <div><strong>Amount:</strong> {{ detail.currency }} {{ detail.amountTotal.toFixed(2) }}</div>
            <div><strong>Paid Amount:</strong> {{ detail.currency }} {{ detail.amountPaid.toFixed(2) }}</div>
            <div><strong>Channel:</strong> {{ detail.channel }}</div>
            <div><strong>Transaction No:</strong> {{ detail.externalTransactionNo || '-' }}</div>
            <div class="md:col-span-2"><strong>Description:</strong> {{ detail.description || '-' }}</div>
            <div class="md:col-span-2"><strong>Remark:</strong> {{ detail.remark || '-' }}</div>
          </div>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header><div class="font-semibold text-slate-900">Transactions</div></template>
          <el-table :data="detail.transactions" border size="small" class="overflow-hidden rounded-2xl">
            <el-table-column prop="transactionType" label="Type" min-width="140" />
            <el-table-column prop="transactionStatus" label="Status" min-width="140" />
            <el-table-column prop="externalTransactionNo" label="External Transaction No" min-width="180" />
            <el-table-column prop="externalOutTradeNo" label="Out Trade No" min-width="180" />
            <el-table-column prop="successTime" label="Success Time" min-width="180" />
          </el-table>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header><div class="font-semibold text-slate-900">Notify Logs</div></template>
          <el-table :data="detail.notifyLogs" border size="small" class="overflow-hidden rounded-2xl">
            <el-table-column prop="notifyType" label="Type" min-width="140" />
            <el-table-column prop="notifyStatus" label="Status" min-width="140" />
            <el-table-column prop="resourceId" label="Resource Id" min-width="160" />
            <el-table-column prop="processResult" label="Result" min-width="220" />
            <el-table-column prop="createdAt" label="Created At" min-width="180" />
            <el-table-column label="Actions" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  size="small"
                  :disabled="!isReplayablePaymentNotify(row.notifyStatus)"
                  @click="onReplayPaymentNotify(row.id)"
                >
                  Replay
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header><div class="font-semibold text-slate-900">Refunds</div></template>
          <el-table :data="detail.refunds" border size="small" class="overflow-hidden rounded-2xl">
            <el-table-column prop="refundNo" label="Refund No" min-width="180" />
            <el-table-column prop="amountRefund" label="Amount" min-width="120" />
            <el-table-column prop="status" label="Status" min-width="140" />
            <el-table-column prop="externalRefundNo" label="External Refund No" min-width="180" />
            <el-table-column prop="refundedAt" label="Refunded At" min-width="180" />
            <el-table-column label="Actions" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  size="small"
                  :disabled="row.status !== 'failed'"
                  @click="onRetryRefund(row.id)"
                >
                  Retry
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="rounded-2xl border border-slate-200">
          <template #header><div class="font-semibold text-slate-900">Refund Notify Logs</div></template>
          <el-table :data="detail.refundNotifyLogs" border size="small" class="overflow-hidden rounded-2xl">
            <el-table-column prop="notifyType" label="Type" min-width="140" />
            <el-table-column prop="notifyStatus" label="Status" min-width="140" />
            <el-table-column prop="resourceId" label="Resource Id" min-width="160" />
            <el-table-column prop="processResult" label="Result" min-width="220" />
            <el-table-column prop="createdAt" label="Created At" min-width="180" />
            <el-table-column label="Actions" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  size="small"
                  :disabled="!isReplayableRefundNotify(row.notifyStatus)"
                  @click="onReplayRefundNotify(row.id)"
                >
                  Replay
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-dialog>

    <el-dialog v-model="refundDialogVisible" title="Create Refund" width="520px" destroy-on-close @closed="onRefundDialogClosed">
      <el-form ref="refundFormRef" :model="refundForm" label-position="top">
        <el-form-item label="Refund Amount">
          <el-input-number v-model="refundForm.amountRefund" :min="0.01" :precision="2" class="!w-full" />
        </el-form-item>
        <el-form-item label="Reason">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="refundDialogVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="saving" @click="onCreateRefund">Submit Refund</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="reconcileDialogVisible" title="New Reconcile Record" width="560px" destroy-on-close @closed="onReconcileDialogClosed">
      <el-form :model="reconcileForm" label-position="top">
        <el-form-item label="Reconcile Date">
          <el-input v-model="reconcileForm.reconcileDate" placeholder="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="Channel">
          <el-select v-model="reconcileForm.channel">
            <el-option v-for="item in channelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="Status">
          <el-input v-model="reconcileForm.reconcileStatus" />
        </el-form-item>
        <el-form-item label="Diff Count">
          <el-input-number v-model="reconcileForm.diffCount" :min="0" class="!w-full" />
        </el-form-item>
        <el-form-item label="Summary">
          <el-input v-model="reconcileForm.summary" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="reconcileDialogVisible = false">Cancel</el-button>
          <el-button type="primary" :loading="saving" @click="onCreateReconcile">Save</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="reconcileDiffDialogVisible"
      title="Reconcile Diff Detail"
      width="760px"
      destroy-on-close
      @closed="onReconcileDiffDialogClosed"
    >
      <div v-if="reconcileDiffDetail" class="space-y-4">
        <div class="grid gap-4 rounded-2xl border border-line bg-white/70 p-4 md:grid-cols-2">
          <div><strong>Date:</strong> {{ reconcileDiffDetail.reconcileDate }}</div>
          <div><strong>Channel:</strong> {{ reconcileDiffDetail.channel }}</div>
          <div><strong>Status:</strong> {{ reconcileDiffDetail.reconcileStatus }}</div>
          <div><strong>Diff Count:</strong> {{ reconcileDiffDetail.diffCount }}</div>
          <div class="md:col-span-2"><strong>Summary:</strong> {{ reconcileDiffDetail.summary || '-' }}</div>
        </div>

        <div class="rounded-2xl border border-line bg-white/70 p-4">
          <div class="mb-3 text-sm font-semibold text-ink">Diff Items</div>
          <div v-if="reconcileDiffDetail.diffItems.length" class="space-y-2">
            <div
              v-for="(item, index) in reconcileDiffDetail.diffItems"
              :key="`${reconcileDiffDetail.id}-${index}`"
              class="rounded-xl border border-line px-3 py-2 text-sm text-ink"
            >
              {{ item }}
            </div>
          </div>
          <el-empty v-else description="No diff items" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
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
  fetchPaymentOpsOverview,
  fetchPayment,
  fetchPaymentMerchants,
  fetchPayments,
  fetchReconcileDiffDetail,
  fetchReconcileRecords,
  replayPaymentNotifyLog,
  replayRefundNotifyLog,
  refreshCurrentMerchantCertificate,
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
  ReconcileDiffDetail,
  ReconcileCreatePayload,
  ReconcileRecord,
  RefundCreatePayload,
} from '../types/payment'
import type { WaybillSummary } from '../types/waybill'

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
  memberId: [{ required: true, message: 'Please select a member', trigger: 'change' }],
  channel: [{ required: true, message: 'Please select a channel', trigger: 'change' }],
  amountTotal: [{ required: true, message: 'Please enter an amount', trigger: 'change' }],
}

const merchantRules: FormRules = {
  merchantName: [{ required: true, message: 'Please enter merchant name', trigger: 'blur' }],
  merchantCode: [{ required: true, message: 'Please enter merchant code', trigger: 'blur' }],
  mchId: [{ required: true, message: 'Please enter mch id', trigger: 'blur' }],
  appId: [{ required: true, message: 'Please enter app id', trigger: 'blur' }],
  notifyUrl: [{ required: true, message: 'Please enter notify url', trigger: 'blur' }],
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
  } catch {
    ElMessage.error('Failed to load payment management data.')
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
  } catch {
    ElMessage.error('Failed to load payment detail.')
  } finally {
    loading.value = false
  }
}

async function openReconcileDiffDialog(id: number) {
  loading.value = true
  try {
    reconcileDiffDetail.value = await fetchReconcileDiffDetail(id)
    reconcileDiffDialogVisible.value = true
  } catch {
    ElMessage.error('Failed to load reconcile diff detail.')
  } finally {
    loading.value = false
  }
}

async function onSaveMerchant() {
  const valid = merchantFormRef.value
    ? await merchantFormRef.value.validate().then(() => true).catch(() => false)
    : true
  if (!valid) return

  saving.value = true
  try {
    if (merchantEditingId.value) {
      await updatePaymentMerchant(merchantEditingId.value, merchantForm)
      ElMessage.success('Merchant updated successfully.')
    } else {
      await createPaymentMerchant(merchantForm)
      ElMessage.success('Merchant created successfully.')
    }
    merchantDialogVisible.value = false
    await loadMerchantConfigs()
  } catch {
    ElMessage.error('Failed to save merchant.')
  } finally {
    saving.value = false
  }
}

async function onActivateMerchant(id: number) {
  try {
    await ElMessageBox.confirm('Switch the active merchant for all new payment orders?', 'Activate Merchant', {
      type: 'warning',
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
    })
  } catch {
    return
  }

  try {
    await activatePaymentMerchant(id)
    ElMessage.success('Active merchant switched successfully.')
    await Promise.all([loadMerchantConfigs(), loadOpsOverview()])
  } catch {
    ElMessage.error('Failed to switch merchant.')
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
    ElMessage.success('Merchant certificate refreshed successfully.')
  } catch {
    ElMessage.error('Failed to refresh merchant certificate.')
  } finally {
    certificateRefreshing.value = false
  }
}

async function onReplayPaymentNotify(id: number) {
  if (!detail.value?.id) return

  saving.value = true
  try {
    await replayPaymentNotifyLog(id)
    detail.value = await fetchPayment(detail.value.id)
    await Promise.all([loadPayments(), loadOpsOverview()])
    ElMessage.success('Payment callback replayed successfully.')
  } catch {
    ElMessage.error('Failed to replay payment callback.')
  } finally {
    saving.value = false
  }
}

async function onReplayRefundNotify(id: number) {
  if (!detail.value?.id) return

  saving.value = true
  try {
    await replayRefundNotifyLog(id)
    detail.value = await fetchPayment(detail.value.id)
    await Promise.all([loadPayments(), loadOpsOverview()])
    ElMessage.success('Refund callback replayed successfully.')
  } catch {
    ElMessage.error('Failed to replay refund callback.')
  } finally {
    saving.value = false
  }
}

async function onRetryRefund(id: number) {
  if (!detail.value?.id) return

  saving.value = true
  try {
    await retryRefund(id)
    detail.value = await fetchPayment(detail.value.id)
    await Promise.all([loadPayments(), loadOpsOverview()])
    ElMessage.success('Refund retried successfully.')
  } catch {
    ElMessage.error('Failed to retry refund.')
  } finally {
    saving.value = false
  }
}

async function onCreatePayment() {
  const valid = formRef.value
    ? await formRef.value.validate().then(() => true).catch(() => false)
    : true
  if (!valid) return

  saving.value = true
  try {
    await createPayment(form)
    ElMessage.success('Payment created successfully.')
    createDialogVisible.value = false
    await loadPayments()
  } catch {
    ElMessage.error('Failed to create payment.')
  } finally {
    saving.value = false
  }
}

async function onChangeStatus(id: number, status: string) {
  const actionText = status === 'paid' ? 'mark this payment as paid' : 'close this payment'
  try {
    await ElMessageBox.confirm(`Are you sure you want to ${actionText}?`, 'Update Payment', {
      type: 'warning',
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
    })
  } catch {
    return
  }

  try {
    await updatePaymentStatus(id, status)
    ElMessage.success('Payment status updated successfully.')
    await loadPayments()
    if (detail.value?.id === id) {
      detail.value = await fetchPayment(id)
    }
  } catch {
    ElMessage.error('Failed to update payment status.')
  }
}

async function onCreateRefund() {
  if (!selectedPaymentId.value || !refundForm.amountRefund) {
    ElMessage.error('Refund amount is required.')
    return
  }

  saving.value = true
  try {
    await createRefund(selectedPaymentId.value, refundForm)
    ElMessage.success('Refund created successfully.')
    refundDialogVisible.value = false
    await loadPayments()
    if (detail.value?.id === selectedPaymentId.value) {
      detail.value = await fetchPayment(selectedPaymentId.value)
    }
  } catch {
    ElMessage.error('Failed to create refund.')
  } finally {
    saving.value = false
  }
}

async function onCreateReconcile() {
  saving.value = true
  try {
    await createReconcileRecord(reconcileForm)
    ElMessage.success('Reconcile record saved successfully.')
    reconcileDialogVisible.value = false
    await loadReconcileRecords()
  } catch {
    ElMessage.error('Failed to save reconcile record.')
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
