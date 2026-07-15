const TOKEN_KEY = 'member_token'
const TOKEN_TYPE_KEY = 'member_token_type'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function getTokenType() {
  return uni.getStorageSync(TOKEN_TYPE_KEY) || 'Bearer'
}

export function setToken(token, tokenType = 'Bearer') {
  uni.setStorageSync(TOKEN_KEY, token)
  uni.setStorageSync(TOKEN_TYPE_KEY, tokenType)
}

export function clearToken() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(TOKEN_TYPE_KEY)
}

export function isLoggedIn() {
  return !!getToken()
}
