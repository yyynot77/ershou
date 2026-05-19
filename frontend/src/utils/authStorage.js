/**
 * 使用 sessionStorage：每个浏览器标签页独立登录态，便于演示时同时开管理员/商家/买家三个页面。
 */
const TOKEN_KEY = 'token'
const USER_KEY = 'user'
const MERCHANT_KEY = 'merchant'

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY) || ''
}

export function setAuth(data) {
  sessionStorage.setItem(TOKEN_KEY, data.token)
  sessionStorage.setItem(USER_KEY, JSON.stringify(data.user))
  if (data.merchant) sessionStorage.setItem(MERCHANT_KEY, JSON.stringify(data.merchant))
  else sessionStorage.removeItem(MERCHANT_KEY)
}

export function clearAuth() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(MERCHANT_KEY)
}

export function loadUser() {
  try {
    return JSON.parse(sessionStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

export function loadMerchant() {
  try {
    return JSON.parse(sessionStorage.getItem(MERCHANT_KEY) || 'null')
  } catch {
    return null
  }
}
