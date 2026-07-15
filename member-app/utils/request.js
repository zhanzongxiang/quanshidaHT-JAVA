import { API_BASE_URL } from './config'
import { getToken, getTokenType } from './auth'

export function request(options) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const tokenType = getTokenType()
    uni.request({
      url: `${API_BASE_URL}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `${tokenType} ${token}` } : {}),
        ...(options.header || {}),
      },
      success: (res) => {
        const body = res.data
        if (res.statusCode >= 200 && res.statusCode < 300 && body?.code === 0) {
          resolve(body.data)
          return
        }
        reject(new Error(body?.message || '请求失败'))
      },
      fail: reject,
    })
  })
}
