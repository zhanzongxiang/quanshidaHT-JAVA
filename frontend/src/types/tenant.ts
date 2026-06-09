export interface TenantDomainItem {
  id: number
  domain: string
  domainType: string
  enabled: boolean
  createdAt: string | null
}

export interface TenantBootstrapAdminInfo {
  username: string
  initialPassword: string
}

export interface TenantDomainPayload {
  domain: string
  domainType: string
  enabled: boolean
}

export interface TenantSummary {
  id: number
  tenantCode: string
  tenantName: string
  status: string
  timezone: string
  locale: string
  remark: string
  domains: TenantDomainItem[]
  bootstrapAdmin: TenantBootstrapAdminInfo | null
  createdAt: string | null
  updatedAt: string | null
}

export interface TenantSavePayload {
  tenantCode: string
  tenantName: string
  status: string
  timezone: string
  locale: string
  remark: string
  domains: TenantDomainPayload[]
}
