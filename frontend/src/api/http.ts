import axios from 'axios'
import type { AxiosError } from 'axios'
import type { ApiResponse } from './shared'

export const http = axios.create({
  baseURL: '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('access_token')
    }

    const message =
      error.response?.data?.message?.trim() ||
      error.message?.trim() ||
      '请求失败，请稍后重试'

    return Promise.reject(new Error(message))
  },
)
