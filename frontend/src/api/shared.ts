import type { AxiosRequestConfig, AxiosResponse } from 'axios'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const API_ERROR_CODE = {
  validationFailed: 40001,
  invalidArgument: 40002,
  tenantContextRequired: 40010,
  authenticationRequired: 40100,
  authenticationFailed: 40101,
  sessionInvalid: 40102,
  authorizationDenied: 40300,
  resourceNotFound: 40400,
  resourceConflict: 40900,
  stateInvalid: 40901,
  rateLimited: 42900,
  internalError: 50000,
} as const

export class ApiError extends Error {
  code?: number
  statusCode?: number

  constructor(message: string, options?: { code?: number; statusCode?: number }) {
    super(message)
    this.name = 'ApiError'
    this.code = options?.code
    this.statusCode = options?.statusCode
  }
}

export function isAuthErrorCode(code?: number) {
  return typeof code === 'number' && code >= API_ERROR_CODE.authenticationRequired && code < 40200
}

type MaybeRecord = Record<string, unknown>

function cleanParams<T extends MaybeRecord>(params?: T): T | undefined {
  if (!params) {
    return undefined
  }

  const normalized = Object.fromEntries(
    Object.entries(params).filter(([, value]) => {
      if (value == null) {
        return false
      }
      if (typeof value === 'string') {
        return value.trim().length > 0
      }
      if (Array.isArray(value)) {
        return value.length > 0
      }
      return true
    }),
  ) as T

  return Object.keys(normalized).length > 0 ? normalized : undefined
}

export function withQuery<T extends MaybeRecord>(params?: T): AxiosRequestConfig | undefined {
  const normalized = cleanParams(params)
  return normalized ? { params: normalized } : undefined
}

export function unwrapResponse<T>(response: AxiosResponse<ApiResponse<T>>) {
  return response.data.data
}
