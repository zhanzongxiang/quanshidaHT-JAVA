import { http } from './http'
import { unwrapResponse } from './shared'
import type { ApiResponse } from './shared'
import type { MeInfo } from '../types/auth'

export interface LoginPayload {
  username: string
  password: string
}

export interface SwitchTenantPayload {
  tenantId: number
}

export async function login(payload: LoginPayload): Promise<void> {
  await http.post<ApiResponse<void>>('/auth/login', payload)
}

export async function logout(): Promise<void> {
  await http.post<ApiResponse<void>>('/auth/logout')
}

export async function switchTenant(payload: SwitchTenantPayload): Promise<void> {
  await http.post<ApiResponse<void>>('/auth/switch-tenant', payload)
}

export async function fetchMe(): Promise<MeInfo> {
  return unwrapResponse(await http.get<ApiResponse<MeInfo>>('/auth/me'))
}
