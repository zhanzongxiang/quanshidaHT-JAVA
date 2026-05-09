import { http } from './http'
import type {
  MerchantCertificateStatus,
  NotifyReplayResult,
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
  RefundOrder,
} from '../types/payment'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export function createEmptyPaymentPayload(): PaymentCreatePayload {
  return {
    memberId: null,
    merchantConfigId: null,
    waybillId: null,
    businessType: 'waybill',
    sceneType: 'mini_program',
    channel: 'wechat_pay',
    currency: 'CNY',
    amountTotal: null,
    description: '',
    remark: '',
  }
}

export function createEmptyRefundPayload(): RefundCreatePayload {
  return {
    amountRefund: null,
    reason: '',
  }
}

export function createEmptyReconcilePayload(): ReconcileCreatePayload {
  return {
    reconcileDate: '',
    channel: 'wechat_pay',
    reconcileStatus: 'pending',
    diffCount: 0,
    summary: '',
  }
}

export function createEmptyMerchantPayload(): PayMerchantConfigPayload {
  return {
    merchantName: '',
    merchantCode: '',
    mchId: '',
    appId: '',
    appSecret: '',
    notifyUrl: '',
    apiV3Key: '',
    privateKeyPath: '',
    merchantSerialNo: '',
    platformCertificatePath: '',
    enabled: true,
    remark: '',
  }
}

export async function fetchPayments(params?: {
  keyword?: string
  status?: string
  channel?: string
}): Promise<PaymentAdminSummary[]> {
  const { data } = await http.get<ApiResponse<PaymentAdminSummary[]>>('/admin/payments', { params })
  return data.data
}

export async function fetchPayment(id: number): Promise<PaymentAdminDetail> {
  const { data } = await http.get<ApiResponse<PaymentAdminDetail>>(`/admin/payments/${id}`)
  return data.data
}

export async function createPayment(payload: PaymentCreatePayload): Promise<PaymentAdminDetail> {
  const { data } = await http.post<ApiResponse<PaymentAdminDetail>>('/admin/payments', payload)
  return data.data
}

export async function updatePaymentStatus(id: number, status: string, externalTransactionNo = ''): Promise<PaymentAdminDetail> {
  const { data } = await http.put<ApiResponse<PaymentAdminDetail>>(`/admin/payments/${id}/status`, {
    status,
    externalTransactionNo,
  })
  return data.data
}

export async function createRefund(id: number, payload: RefundCreatePayload): Promise<RefundOrder> {
  const { data } = await http.post<ApiResponse<RefundOrder>>(`/admin/payments/${id}/refunds`, payload)
  return data.data
}

export async function retryRefund(id: number): Promise<RefundOrder> {
  const { data } = await http.post<ApiResponse<RefundOrder>>(`/admin/payments/refunds/${id}/retry`)
  return data.data
}

export async function fetchReconcileRecords(channel?: string): Promise<ReconcileRecord[]> {
  const { data } = await http.get<ApiResponse<ReconcileRecord[]>>('/admin/payments/reconcile-records', {
    params: { channel },
  })
  return data.data
}

export async function createReconcileRecord(payload: ReconcileCreatePayload): Promise<ReconcileRecord> {
  const { data } = await http.post<ApiResponse<ReconcileRecord>>('/admin/payments/reconcile-records', payload)
  return data.data
}

export async function fetchPaymentOpsOverview(): Promise<PaymentOpsOverview> {
  const { data } = await http.get<ApiResponse<PaymentOpsOverview>>('/admin/payments/ops/overview')
  return data.data
}

export async function refreshCurrentMerchantCertificate(): Promise<MerchantCertificateStatus> {
  const { data } = await http.post<ApiResponse<MerchantCertificateStatus>>('/admin/payments/ops/certificate-refresh')
  return data.data
}

export async function fetchReconcileDiffDetail(id: number): Promise<ReconcileDiffDetail> {
  const { data } = await http.get<ApiResponse<ReconcileDiffDetail>>(`/admin/payments/reconcile-records/${id}/diffs`)
  return data.data
}

export async function replayPaymentNotifyLog(id: number): Promise<NotifyReplayResult> {
  const { data } = await http.post<ApiResponse<NotifyReplayResult>>(`/admin/payments/notify-logs/${id}/replay`)
  return data.data
}

export async function replayRefundNotifyLog(id: number): Promise<NotifyReplayResult> {
  const { data } = await http.post<ApiResponse<NotifyReplayResult>>(`/admin/payments/refund-notify-logs/${id}/replay`)
  return data.data
}

export async function fetchPaymentMerchants(): Promise<PayMerchantConfig[]> {
  const { data } = await http.get<ApiResponse<PayMerchantConfig[]>>('/admin/payment-merchants')
  return data.data
}

export async function createPaymentMerchant(payload: PayMerchantConfigPayload): Promise<PayMerchantConfig> {
  const { data } = await http.post<ApiResponse<PayMerchantConfig>>('/admin/payment-merchants', payload)
  return data.data
}

export async function updatePaymentMerchant(id: number, payload: PayMerchantConfigPayload): Promise<PayMerchantConfig> {
  const { data } = await http.put<ApiResponse<PayMerchantConfig>>(`/admin/payment-merchants/${id}`, payload)
  return data.data
}

export async function activatePaymentMerchant(merchantConfigId: number): Promise<PayMerchantConfig> {
  const { data } = await http.put<ApiResponse<PayMerchantConfig>>('/admin/payment-merchants/activate', { merchantConfigId })
  return data.data
}
