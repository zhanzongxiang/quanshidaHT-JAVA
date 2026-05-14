import { http } from './http'
import { unwrapResponse } from './shared'
import type { ApiResponse } from './shared'
import type { MeInfo } from '../types/auth'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  tokenType: string
}

export async function login(payload: LoginPayload): Promise<LoginResult> {
  return unwrapResponse(await http.post<ApiResponse<LoginResult>>('/auth/login', payload))
}

export async function fetchMe(): Promise<MeInfo> {
  return unwrapResponse(await http.get<ApiResponse<MeInfo>>('/auth/me'))
}
