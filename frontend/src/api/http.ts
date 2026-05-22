import axios from 'axios'
import type { AxiosError } from 'axios'
import type { ApiResponse } from './shared'

export const http = axios.create({
  baseURL: '/api',
  timeout: 10_000,
  withCredentials: true, // send httpOnly cookie with every request
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const message =
      error.response?.data?.message?.trim() ||
      error.message?.trim() ||
      '请求失败，请稍后重试'

    return Promise.reject(new Error(message))
  },
)
