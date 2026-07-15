export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const API_ERROR_CODE = {
  authenticationRequired: 40100,
  authenticationFailed: 40101,
  sessionInvalid: 40102,
} as const

export function isAuthErrorCode(code?: number) {
  return typeof code === 'number' && code >= API_ERROR_CODE.authenticationRequired && code < 40200
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
}
