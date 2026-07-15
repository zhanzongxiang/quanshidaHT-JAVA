import { http } from './http'
import type {
  AdminMemberOrder,
  AdminMemberPackage,
  AdminMemberPrealert,
  AdminMemberShipment,
  AdminPackageInboundPayload,
  AdminShipmentQuotePayload,
} from '../types/member-operations'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface ListParams {
  keyword?: string
  status?: string
}

export async function fetchAdminPrealerts(params?: ListParams): Promise<AdminMemberPrealert[]> {
  const { data } = await http.get<ApiResponse<AdminMemberPrealert[]>>('/admin/member-operations/prealerts', { params })
  return data.data
}

export async function inboundAdminPrealert(
  id: number,
  payload: AdminPackageInboundPayload,
): Promise<AdminMemberPackage> {
  const { data } = await http.post<ApiResponse<AdminMemberPackage>>(
    `/admin/member-operations/prealerts/${id}/inbound`,
    payload,
  )
  return data.data
}

export async function fetchAdminPackages(params?: ListParams): Promise<AdminMemberPackage[]> {
  const { data } = await http.get<ApiResponse<AdminMemberPackage[]>>('/admin/member-operations/packages', { params })
  return data.data
}

export async function fetchAdminShipments(params?: ListParams): Promise<AdminMemberShipment[]> {
  const { data } = await http.get<ApiResponse<AdminMemberShipment[]>>('/admin/member-operations/shipments', { params })
  return data.data
}

export async function quoteAdminShipment(
  id: number,
  payload: AdminShipmentQuotePayload,
): Promise<AdminMemberShipment> {
  const { data } = await http.post<ApiResponse<AdminMemberShipment>>(
    `/admin/member-operations/shipments/${id}/quote`,
    payload,
  )
  return data.data
}

export async function updateAdminShipmentStatus(id: number, status: string): Promise<AdminMemberShipment> {
  const { data } = await http.put<ApiResponse<AdminMemberShipment>>(
    `/admin/member-operations/shipments/${id}/status`,
    { status },
  )
  return data.data
}

export async function fetchAdminOrders(params?: ListParams): Promise<AdminMemberOrder[]> {
  const { data } = await http.get<ApiResponse<AdminMemberOrder[]>>('/admin/member-operations/orders', { params })
  return data.data
}

export async function markAdminOrderPaid(id: number): Promise<AdminMemberOrder> {
  const { data } = await http.post<ApiResponse<AdminMemberOrder>>(
    `/admin/member-operations/orders/${id}/mark-paid`,
  )
  return data.data
}
