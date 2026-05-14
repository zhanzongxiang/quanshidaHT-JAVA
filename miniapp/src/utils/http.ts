import { API_BASE_URL } from '@/config/env'
import type { ApiResponse } from '@/types/common'
import { setLastApiEvent } from '@/utils/debug'
import { normalizeMessage, showError } from '@/utils/toast'

const TOKEN_STORAGE_KEY = 'member_token'
const NETWORK_ERROR_MESSAGE = '网络请求失败，请检查服务是否可用'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'
type RequestData = string | AnyObject | ArrayBuffer | undefined

interface RequestOptions<T> {
  url: string
  method?: HttpMethod
  data?: unknown
  auth?: boolean
}

function logApiEvent(type: 'success' | 'error', options: {
  url: string
  method: HttpMethod
  message: string
  statusCode?: number
}) {
  setLastApiEvent({
    type,
    url: options.url,
    method: options.method,
    message: options.message,
    statusCode: options.statusCode,
    at: new Date().toISOString(),
  })
}

export function getStoredToken() {
  return uni.getStorageSync(TOKEN_STORAGE_KEY) || ''
}

export function setStoredToken(token: string) {
  uni.setStorageSync(TOKEN_STORAGE_KEY, token)
}

export function clearStoredToken() {
  uni.removeStorageSync(TOKEN_STORAGE_KEY)
}

export function clearSessionStorage() {
  clearStoredToken()
}

export async function request<T>({ url, method = 'GET', data, auth = true }: RequestOptions<T>): Promise<T> {
  const headers: Record<string, string> = {}
  const token = getStoredToken()

  if (auth && token) {
    headers.Authorization = `Bearer ${token}`
  }

  return new Promise<T>((resolve, reject) => {
    uni.request({
      url: `${API_BASE_URL}${url}`,
      method,
      data: data as RequestData,
      header: headers,
      success: (response) => {
        const body = response.data as ApiResponse<T> | undefined
        if (response.statusCode >= 200 && response.statusCode < 300 && body && body.code === 0) {
          logApiEvent('success', {
            url,
            method,
            message: 'ok',
            statusCode: response.statusCode,
          })
          resolve(body.data)
          return
        }

        const message = normalizeMessage(body?.message, `请求失败（状态码 ${response.statusCode}）`)
        logApiEvent('error', {
          url,
          method,
          message,
          statusCode: response.statusCode,
        })

        if (response.statusCode === 401) {
          clearSessionStorage()
        }

        showError(message)
        reject(new Error(message))
      },
      fail: (error) => {
        logApiEvent('error', {
          url,
          method,
          message: NETWORK_ERROR_MESSAGE,
        })
        showError(NETWORK_ERROR_MESSAGE)
        reject(error instanceof Error ? error : new Error(NETWORK_ERROR_MESSAGE))
      },
    })
  })
}
