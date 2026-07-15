import { http } from './http'
import { unwrapResponse } from './shared'
import type { ApiResponse } from './shared'
import type { TenantDomainItem, TenantSavePayload, TenantSummary } from '../types/tenant'

interface TenantDomainApiModel {
  id: number
  domain: string
  domainType: string
  enabled: boolean
  createdAt: string | null
}

interface TenantApiModel {
  id: number
  tenantCode: string
  tenantName: string
  status: string
  timezone: string
  locale: string
  remark: string | null
  domains: TenantDomainApiModel[] | null
  bootstrapAdmin: {
    username: string
    initialPassword: string
  } | null
  createdAt: string | null
  updatedAt: string | null
}

function toDomain(model: TenantDomainApiModel): TenantDomainItem {
  return {
    id: model.id,
    domain: model.domain,
    domainType: model.domainType,
    enabled: model.enabled,
    createdAt: model.createdAt,
  }
}

function toSummary(model: TenantApiModel): TenantSummary {
  return {
    id: model.id,
    tenantCode: model.tenantCode,
    tenantName: model.tenantName,
    status: model.status,
    timezone: model.timezone,
    locale: model.locale,
    remark: model.remark ?? '',
    domains: (model.domains ?? []).map(toDomain),
    bootstrapAdmin: model.bootstrapAdmin
      ? {
          username: model.bootstrapAdmin.username,
          initialPassword: model.bootstrapAdmin.initialPassword,
        }
      : null,
    createdAt: model.createdAt,
    updatedAt: model.updatedAt,
  }
}

export function createEmptyTenantPayload(): TenantSavePayload {
  return {
    tenantCode: '',
    tenantName: '',
    status: 'ACTIVE',
    timezone: 'Asia/Shanghai',
    locale: 'zh-CN',
    remark: '',
    domains: [],
  }
}

export async function fetchCurrentTenant(): Promise<TenantSummary> {
  return toSummary(unwrapResponse(await http.get<ApiResponse<TenantApiModel>>('/platform/tenants/current')))
}

export async function fetchTenants(): Promise<TenantSummary[]> {
  return unwrapResponse(await http.get<ApiResponse<TenantApiModel[]>>('/platform/tenants')).map(toSummary)
}

export async function createTenant(payload: TenantSavePayload): Promise<TenantSummary> {
  return toSummary(unwrapResponse(await http.post<ApiResponse<TenantApiModel>>('/platform/tenants', payload)))
}

export async function updateTenant(id: number, payload: TenantSavePayload): Promise<TenantSummary> {
  return toSummary(unwrapResponse(await http.put<ApiResponse<TenantApiModel>>(`/platform/tenants/${id}`, payload)))
}
