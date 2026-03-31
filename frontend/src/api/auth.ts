import { http } from './http'
import type { MeInfo } from '../types/auth'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  tokenType: string
}

export async function login(payload: LoginPayload): Promise<LoginResult> {
  const { data } = await http.post<ApiResponse<LoginResult>>('/auth/login', payload)
  return data.data
}

export async function fetchMe(): Promise<MeInfo> {
  const { data } = await http.get<ApiResponse<MeInfo>>('/auth/me')
  return data.data
}
