import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const merchant = ref(JSON.parse(localStorage.getItem('merchant') || 'null'))

  function setLogin(data) {
    token.value = data.token
    user.value = data.user
    merchant.value = data.merchant || null
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    if (data.merchant) localStorage.setItem('merchant', JSON.stringify(data.merchant))
    else localStorage.removeItem('merchant')
  }

  function logout() {
    token.value = ''
    user.value = null
    merchant.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('merchant')
  }

  const isLogin = () => !!token.value
  const isAdmin = () => user.value?.role === 'ADMIN'
  const isMerchant = () => user.value?.role === 'MERCHANT'

  return { token, user, merchant, setLogin, logout, isLogin, isAdmin, isMerchant }
})
