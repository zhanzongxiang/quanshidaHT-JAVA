export interface PaymentAdminSummary {
  id: number
  orderNo: string
  memberId: number
  merchantConfigId: number | null
  merchantName: string
  merchantMchId: string
  merchantAppId: string
  memberPhone: string
  waybillId: number | null
  waybillTrackingNo: string
  businessType: string
  sceneType: string
  channel: string
  currency: string
  amountTotal: number
  amountPaid: number
  status: string
  externalTransactionNo: string
  paidAt: string | null
  createdAt: string
}

export interface PaymentTransaction {
  id: number
  transactionType: string
  transactionStatus: string
  externalTransactionNo: string
  externalOutTradeNo: string
  successTime: string | null
  createdAt: string
}

export interface RefundOrder {
  id: number
  refundNo: string
  amountRefund: number
  status: string
  reason: string
  externalRefundNo: string
  refundedAt: string | null
  createdAt: string
}

export interface ReconcileRecord {
  id: number
  reconcileDate: string
  channel: string
  reconcileStatus: string
  diffCount: number
  summary: string
  createdAt: string
  updatedAt: string
}

export interface MerchantCertificateStatus {
  merchantConfigId: number
  merchantName: string
  mchId: string
  certificatePath: string
  autoRefreshEnabled: boolean
  lastUpdatedAt: string | null
}

export interface NotifyFailureStat {
  category: string
  count: number
  latestCreatedAt: string | null
}

export interface PaymentOpsOverview {
  currentMerchantCertificate: MerchantCertificateStatus
  paymentNotifyFailures: NotifyFailureStat[]
  refundNotifyFailures: NotifyFailureStat[]
}

export interface ReconcileDiffDetail {
  id: number
  reconcileDate: string
  channel: string
  reconcileStatus: string
  diffCount: number
  diffItems: string[]
  summary: string
}

export interface PaymentNotifyLog {
  id: number
  notifyType: string
  resourceId: string
  notifyStatus: string
  processResult: string
  notifiedAt: string | null
  createdAt: string
}

export interface RefundNotifyLog {
  id: number
  notifyType: string
  resourceId: string
  notifyStatus: string
  processResult: string
  notifiedAt: string | null
  createdAt: string
}

export interface NotifyReplayResult {
  sourceLogId: number
  replayType: string
  replayStatus: string
  message: string
}

export interface PaymentAdminDetail extends PaymentAdminSummary {
  memberNickname: string
  description: string
  expiredAt: string | null
  closedAt: string | null
  refundedAt: string | null
  remark: string
  updatedAt: string
  transactions: PaymentTransaction[]
  refunds: RefundOrder[]
  notifyLogs: PaymentNotifyLog[]
  refundNotifyLogs: RefundNotifyLog[]
}

export interface PaymentCreatePayload {
  memberId: number | null
  merchantConfigId: number | null
  waybillId: number | null
  businessType: string
  sceneType: string
  channel: string
  currency: string
  amountTotal: number | null
  description: string
  remark: string
}

export interface RefundCreatePayload {
  amountRefund: number | null
  reason: string
}

export interface ReconcileCreatePayload {
  reconcileDate: string
  channel: string
  reconcileStatus: string
  diffCount: number
  summary: string
}

export interface PayMerchantConfig {
  id: number
  merchantName: string
  merchantCode: string
  mchId: string
  appId: string
  appSecretConfigured: boolean
  notifyUrl: string
  apiV3KeyConfigured: boolean
  privateKeyConfigured: boolean
  merchantSerialNoConfigured: boolean
  platformCertificateConfigured: boolean
  configurationReady: boolean
  configurationStatus: string
  configurationIssues: string[]
  enabled: boolean
  active: boolean
  remark: string
  createdAt: string
  updatedAt: string
}

export interface PayMerchantConfigPayload {
  merchantName: string
  merchantCode: string
  mchId: string
  appId: string
  appSecret: string
  notifyUrl: string
  apiV3Key: string
  privateKeyPath: string
  merchantSerialNo: string
  platformCertificatePath: string
  enabled: boolean
  remark: string
}
