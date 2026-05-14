import { http } from './http'
import { unwrapResponse, withQuery } from './shared'
import type { ApiResponse } from './shared'
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
  return unwrapResponse(await http.get<ApiResponse<PaymentAdminSummary[]>>('/admin/payments', withQuery(params)))
}

export async function fetchPayment(id: number): Promise<PaymentAdminDetail> {
  return unwrapResponse(await http.get<ApiResponse<PaymentAdminDetail>>(`/admin/payments/${id}`))
}

export async function createPayment(payload: PaymentCreatePayload): Promise<PaymentAdminDetail> {
  return unwrapResponse(await http.post<ApiResponse<PaymentAdminDetail>>('/admin/payments', payload))
}

export async function updatePaymentStatus(id: number, status: string, externalTransactionNo = ''): Promise<PaymentAdminDetail> {
  return unwrapResponse(
    await http.put<ApiResponse<PaymentAdminDetail>>(`/admin/payments/${id}/status`, {
      status,
      externalTransactionNo,
    }),
  )
}

export async function createRefund(id: number, payload: RefundCreatePayload): Promise<RefundOrder> {
  return unwrapResponse(await http.post<ApiResponse<RefundOrder>>(`/admin/payments/${id}/refunds`, payload))
}

export async function retryRefund(id: number): Promise<RefundOrder> {
  return unwrapResponse(await http.post<ApiResponse<RefundOrder>>(`/admin/payments/refunds/${id}/retry`))
}

export async function fetchReconcileRecords(channel?: string): Promise<ReconcileRecord[]> {
  return unwrapResponse(
    await http.get<ApiResponse<ReconcileRecord[]>>('/admin/payments/reconcile-records', withQuery({ channel })),
  )
}

export async function createReconcileRecord(payload: ReconcileCreatePayload): Promise<ReconcileRecord> {
  return unwrapResponse(await http.post<ApiResponse<ReconcileRecord>>('/admin/payments/reconcile-records', payload))
}

export async function fetchPaymentOpsOverview(): Promise<PaymentOpsOverview> {
  return unwrapResponse(await http.get<ApiResponse<PaymentOpsOverview>>('/admin/payments/ops/overview'))
}

export async function refreshCurrentMerchantCertificate(): Promise<MerchantCertificateStatus> {
  return unwrapResponse(await http.post<ApiResponse<MerchantCertificateStatus>>('/admin/payments/ops/certificate-refresh'))
}

export async function fetchReconcileDiffDetail(id: number): Promise<ReconcileDiffDetail> {
  return unwrapResponse(await http.get<ApiResponse<ReconcileDiffDetail>>(`/admin/payments/reconcile-records/${id}/diffs`))
}

export async function replayPaymentNotifyLog(id: number): Promise<NotifyReplayResult> {
  return unwrapResponse(await http.post<ApiResponse<NotifyReplayResult>>(`/admin/payments/notify-logs/${id}/replay`))
}

export async function replayRefundNotifyLog(id: number): Promise<NotifyReplayResult> {
  return unwrapResponse(await http.post<ApiResponse<NotifyReplayResult>>(`/admin/payments/refund-notify-logs/${id}/replay`))
}

export async function fetchPaymentMerchants(): Promise<PayMerchantConfig[]> {
  return unwrapResponse(await http.get<ApiResponse<PayMerchantConfig[]>>('/admin/payment-merchants'))
}

export async function createPaymentMerchant(payload: PayMerchantConfigPayload): Promise<PayMerchantConfig> {
  return unwrapResponse(await http.post<ApiResponse<PayMerchantConfig>>('/admin/payment-merchants', payload))
}

export async function updatePaymentMerchant(id: number, payload: PayMerchantConfigPayload): Promise<PayMerchantConfig> {
  return unwrapResponse(await http.put<ApiResponse<PayMerchantConfig>>(`/admin/payment-merchants/${id}`, payload))
}

export async function activatePaymentMerchant(merchantConfigId: number): Promise<PayMerchantConfig> {
  return unwrapResponse(await http.put<ApiResponse<PayMerchantConfig>>('/admin/payment-merchants/activate', { merchantConfigId }))
}
