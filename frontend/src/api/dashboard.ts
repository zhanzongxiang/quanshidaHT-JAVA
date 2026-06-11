import { http } from './http'
import { unwrapResponse } from './shared'
import type { ApiResponse } from './shared'

export interface DashboardSummary {
  tenantId: number
  tenantCode: string
  tenantName: string
  tenantStatus: string
  timezone: string
  locale: string
  enabledDomainCount: number
  enabledMerchantCount: number
  memberTotal: number
  memberEnabled: number
  memberWechatBound: number
  waybillTotal: number
  waybillInTransit: number
  payOrderTotal: number
  payOrderPaying: number
  payOrderPaid: number
  refundOrderTotal: number
  refundProcessing: number
  newsTotal: number
  newsPublished: number
  serviceLineTotal: number
  homeContentStatus: string | null
  homeContentUpdatedAt: string | null
}

export async function fetchDashboardSummary(): Promise<DashboardSummary> {
  return unwrapResponse(await http.get<ApiResponse<DashboardSummary>>('/dashboard/summary'))
}
