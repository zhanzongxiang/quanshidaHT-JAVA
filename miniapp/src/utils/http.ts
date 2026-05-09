import { API_BASE_URL } from '@/config/env'
import type { ApiResponse } from '@/types/common'

const TOKEN_STORAGE_KEY = 'member_token'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

type RequestData = string | AnyObject | ArrayBuffer | undefined

interface RequestOptions<T> {
  url: string
  method?: HttpMethod
  data?: unknown
  auth?: boolean
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
          resolve(body.data)
          return
        }

        const message = body?.message || `request failed with status ${response.statusCode}`
        if (response.statusCode === 401) {
          clearStoredToken()
        }
        uni.showToast({
          title: message,
          icon: 'none',
          duration: 2200,
        })
        reject(new Error(message))
      },
      fail: (error) => {
        uni.showToast({
          title: 'network request failed',
          icon: 'none',
          duration: 2200,
        })
        reject(error)
      },
    })
  })
}
