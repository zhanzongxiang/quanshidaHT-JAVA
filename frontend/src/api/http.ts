import axios from 'axios'
import type { AxiosError } from 'axios'
import { ApiError, isAuthErrorCode, type ApiResponse } from './shared'

export const ADMIN_AUTH_EXPIRED_EVENT = 'qsd-admin-auth-expired'

export const http = axios.create({
  baseURL: '/api',
  timeout: 10_000,
  withCredentials: true, // send httpOnly cookie with every request
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    const apiCode = error.response?.data?.code
    const statusCode = error.response?.status
    const message =
      error.response?.data?.message?.trim() ||
      error.message?.trim() ||
      'Request failed, please retry later'

    if (typeof window !== 'undefined' && (isAuthErrorCode(apiCode) || statusCode === 401)) {
      window.dispatchEvent(new CustomEvent(ADMIN_AUTH_EXPIRED_EVENT, {
        detail: {
          code: apiCode,
          statusCode,
          message,
        },
      }))
    }

    return Promise.reject(new ApiError(message, { code: apiCode, statusCode }))
  },
)
