/**
 * ============================================================================
 * 模块：Pinia 购物车角标状态
 * ============================================================================
 *
 * 职责：仅维护「购物车商品总件数」count，用于 MainLayout 顶栏/菜单红点
 * 完整列表数据在 Cart.vue 页面内单独 getCart 拉取，不放入本 store
 *
 * 刷新时机：
 * - MainLayout onMounted / 登录态变化 watch → refresh()
 * - ProductCard/ProductDetail 加购成功后 → refresh()
 * - Cart.vue 删改/结算后 → refresh()
 *
 * 调用链（refresh）：
 * getCart() → GET /api/cart → CartService.list
 * → 前端 reduce 累加 cartItem.quantity → count.value 更新 → el-badge 重渲染
 * ============================================================================
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCart } from '../api'

export const useCartStore = defineStore('cart', () => {
  const count = ref(0)

  /** 从服务端重新统计件数；售罄/下架项由 CartService.list 自动剔除 */
  async function refresh() {
    try {
      const res = await getCart()
      count.value = (res.data || []).reduce((s, i) => s + (i.cartItem?.quantity || 0), 0)
    } catch {
      count.value = 0
    }
  }

  /** 退出登录时 MainLayout 直接置 0，避免未登录仍显示角标 */
  function setCount(n) {
    count.value = n
  }

  return { count, refresh, setCount }
})
