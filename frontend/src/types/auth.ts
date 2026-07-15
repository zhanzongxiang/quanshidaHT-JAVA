export interface AdminMenu {
  id: number
  name: string
  path: string
  component: string
  icon?: string
  children: AdminMenu[]
}

export interface MeInfo {
  userId: number
  username: string
  tenantId: number
  tenantCode: string
  tenantName: string
  loginTenantId: number
  loginTenantCode: string
  loginTenantName: string
  tenantSwitched: boolean
  permissions: string[]
  menus: AdminMenu[]
}
