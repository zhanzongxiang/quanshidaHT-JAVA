import type { AxiosRequestConfig, AxiosResponse } from 'axios'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
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
