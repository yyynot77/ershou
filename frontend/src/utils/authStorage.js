/**
 * ============================================================================
 * 模块：登录态持久化（sessionStorage）
 * ============================================================================
 *
 * 设计原因：
 * - 使用 sessionStorage 而非 localStorage，使每个浏览器标签页独立登录
 * - 便于演示：同一浏览器可同时打开管理员/商家/买家三个标签页互不顶号
 *
 * 写入时机：Login.vue 登录成功 → userStore.setLogin → setAuth
 * 读取时机：
 * - 应用启动：user.js 初始化 token/user
 * - 每次请求：request.js 请求拦截器 getToken
 * 清除时机：logout / 401 响应 / MainLayout.handleCmd('logout')
 *
 * 数据流：
 * 后端 login 返回 { token, user, merchant? }
 *   → setAuth 写入 sessionStorage
 *   → Pinia userStore 同步内存 ref
 * ============================================================================
 */
const TOKEN_KEY = 'token'
const USER_KEY = 'user'
const MERCHANT_KEY = 'merchant'

/** 供 Axios 拦截器读取 JWT 字符串 */
export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 登录成功后持久化
 * @param data 后端 AuthService.buildLoginResult 的返回值
 */
export function setAuth(data) {
  sessionStorage.setItem(TOKEN_KEY, data.token)
  sessionStorage.setItem(USER_KEY, JSON.stringify(data.user))
  if (data.merchant) sessionStorage.setItem(MERCHANT_KEY, JSON.stringify(data.merchant))
  else sessionStorage.removeItem(MERCHANT_KEY)
}

/** 退出或 401 时清空，避免脏 Token 继续发请求 */
export function clearAuth() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
  sessionStorage.removeItem(MERCHANT_KEY)
}

/** 刷新页面后恢复 Pinia user 对象 */
export function loadUser() {
  try {
    return JSON.parse(sessionStorage.getItem(USER_KEY) || 'null')
  } catch {
    return null
  }
}

/** 商家角色额外店铺信息 */
export function loadMerchant() {
  try {
    return JSON.parse(sessionStorage.getItem(MERCHANT_KEY) || 'null')
  } catch {
    return null
  }
}
