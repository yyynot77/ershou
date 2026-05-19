import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCart } from '../api'

export const useCartStore = defineStore('cart', () => {
  const count = ref(0)

  async function refresh() {
    try {
      const res = await getCart()
      count.value = (res.data || []).reduce((s, i) => s + (i.cartItem?.quantity || 0), 0)
    } catch {
      count.value = 0
    }
  }

  function setCount(n) {
    count.value = n
  }

  return { count, refresh, setCount }
})
