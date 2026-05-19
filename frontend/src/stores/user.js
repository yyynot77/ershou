import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setAuth, clearAuth, loadUser, loadMerchant } from '../utils/authStorage'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const user = ref(loadUser())
  const merchant = ref(loadMerchant())

  function setLogin(data) {
    token.value = data.token
    user.value = data.user
    merchant.value = data.merchant || null
    setAuth(data)
  }

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
