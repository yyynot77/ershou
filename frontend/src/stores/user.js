/**
 * ============================================================================
 * 模块：Pinia 用户状态（全局登录态）
 * ============================================================================
 *
 * 使用页面：MainLayout、router 守卫、各业务页权限判断（isUser/isMerchant/isAdmin）
 *
 * 与 authStorage 关系：
 * - 内存：token/user/merchant 三个 ref（响应式，驱动模板更新）
 * - 持久化：authStorage sessionStorage（刷新不丢登录）
 *
 * 登录完整链路：
 * Login.vue submit → api.login → AuthController → AuthService.login
 * → 返回 token → setLogin(data) → 写 Pinia + sessionStorage
 * → router.push('/') → MainLayout 根据 role 显示菜单
 * ============================================================================
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setAuth, clearAuth, loadUser, loadMerchant } from '../utils/authStorage'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const user = ref(loadUser())
  const merchant = ref(loadMerchant())

  /**
   * 登录成功回调（Login.vue 调用）
   * 页面更新：依赖 user/token 的组件（顶栏昵称、路由守卫）自动响应
   */
  function setLogin(data) {
    token.value = data.token
    user.value = data.user
    merchant.value = data.merchant || null
    setAuth(data)
  }

  /** 退出登录：清空内存 + sessionStorage，购物车角标由 MainLayout 置 0 */
  function logout() {
    token.value = ''
    user.value = null
    merchant.value = null
    clearAuth()
  }

  const isLogin = () => !!token.value
  const isAdmin = () => user.value?.role === 'ADMIN'
  const isMerchant = () => user.value?.role === 'MERCHANT'
  const isUser = () => user.value?.role === 'USER'

  return { token, user, merchant, setLogin, logout, isLogin, isAdmin, isMerchant, isUser }
})
