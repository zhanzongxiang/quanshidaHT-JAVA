import { http } from './http'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface DashboardSummary {
  waybillTotal: number
  waybillInTransit: number
  newsTotal: number
  newsPublished: number
  serviceLineTotal: number
  homeContentStatus: string | null
  homeContentUpdatedAt: string | null
}

export async function fetchDashboardSummary(): Promise<DashboardSummary> {
  const { data } = await http.get<ApiResponse<DashboardSummary>>('/dashboard/summary')
  return data.data
}
